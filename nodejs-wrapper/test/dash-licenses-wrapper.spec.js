// Mocha tests for dash-licenses-wrapper
const chai = require('chai');
const should = chai.should();
const expect = chai.expect;
// const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const wrapper = path.resolve("src", "dash-licenses-wrapper.js");

/** configuration types enum */
const configsEnum = {
    /** effective configuration  */
    "EFFECTIVE": "effective",
    /** Parsed CLI  */
    "CLI": "CLI",
    /** Parsed config file   */
    "FILE": "file"
};

/** 
 * Regular expressions to parse wrapper output and extract the different configurations.  
 * In all cases, the matching configuration will be capture in regexp group 1.
 */

// const traceLevelsRegexp = /(INFO||DEBUG):/i;
const configRegexps = {
    /** effective configuration regexp */
    [configsEnum.EFFECTIVE]: /^INFO: Effective configuration:(.*{.*})$/ms,
    /** Parsed CLI regexp  */
    [configsEnum.CLI]: /^DEBUG: Parsed CLI:(.*?{.*?})$/ms,
    /** Parsed config file regexp    */
    [configsEnum.FILE]: /^DEBUG: Parsed config file:(.*?{.*?})$/ms
}

/**
 * Runs the dash-licenses-wrapper and extracts useful info from its output
 * @param {string[]} args CLI arguments to pass to dash-licenses-wrapper
 * @returns 
 */
function runWrapper(args) {
    const command = `${wrapper}`;
    // const opts = { stdio: ['ignore', 'inherit', 'inherit']};
    const opts = { stdio: 'pipe'};
    let status = cp.spawnSync(command, args, opts);

    const output = status.stdout.toString();
    // TODO: handle STDERR trace "Debugger attached.\nWaiting for the debugger to disconnect...\n"
    // that's present when debugging and can interfere with some tests
    const error = status.stderr.toString();
    const cfgFile = extractConfigurationFromStdout(output, configsEnum.FILE);
    const cfgCLI = extractConfigurationFromStdout(output, configsEnum.CLI);
    const cfgFinal = extractConfigurationFromStdout(output, configsEnum.EFFECTIVE);
    
    return [status.status, output, error, cfgFile, cfgCLI, cfgFinal];
}

describe("dash-licenses-wrapper tests", function() {
    describe("test CLI parameters", function() {
        it("test CLI param --help", function() {
            const [status, output, error]  = runWrapper(["--help"]);
            expect(status).to.equal(0);
            // expect(error).to.be.empty;
            expect(output).to.match(/^.*Usage: dash-licenses-wrapper.js \[options\]/);
        });
        it("test output is in color by default", function() {
            const [status, output, error]  = runWrapper(["--help"]);
            expect(status).to.equal(0);
            // expect(error).to.be.empty;
            expect(output).to.match(/^\x1b\[/);
        });
        it("test CLI param --noColor results in no color in output", function() {
            const [status, output, error]  = runWrapper(["--noColor",  "--help"]);
            expect(status).to.equal(0);
            // expect(error).to.be.empty;
            expect(output).to.not.match(/^\x1b\[/);
        });
        it("test CLI param --dryRun results in exit before calling dash-licenses", function() {
            const [status, output, error]  = runWrapper(["--noColor",  "--dryRun"]);
            expect(status).to.equal(0);
            // expect(error).to.be.empty;
            expect(output).to.match(/Dry-run mode enabled/);
        });
        it("test that dry run mode is disabled by default", function() {
            const [status, output, error] = runWrapper(["--noColor", "--help"]);
            expect(status).to.equal(0);
            // expect(error).to.be.empty;
            expect(output).to.not.match(/Dry-run mode enabled/);
        });
        it("test \"--debug\" CLI param enables extra traces", function() {
            // const [status, output, error]  = runWrapper(["--debug", "--noColor", "--dryRun"]);
            const [status, output, error, cfgFile, cfgCLI, cfgFinal] = runWrapper(
                [
                    "--dryRun",
                    "--noColor",
                    "--verbose"
                ]);
            
            expect(status).to.equal(0);
            expect(cfgCLI.dryRun).to.equal(true);
            expect(cfgCLI.noColor).to.equal(true);
            expect(cfgCLI.verbose).to.equal(true);
        });
        it("test \"--configFile\" CLI param works to load configurations from file", function() {
            const [status, output, error, cfgFile, cfgCLI, cfgFinal] = runWrapper(
                [
                    "--noColor",
                    "--dryRun",
                    "--verbose",
                    "--configFile=examples/dashLicensesConfig.json"
                ]);

            expect(status).to.equal(0);
            expect(cfgFinal.configFile).to.equal(cfgCLI.configFile).to.equal("examples/dashLicensesConfig.json");
            expect(cfgFinal.project).to.equal(cfgFile.project).to.equal("ecd.cdt-cloud");
            expect(cfgFinal.inputFile).to.equal(cfgFile.inputFile).to.equal("examples/package-lock.json");
            expect(cfgFinal.batch).to.equal(cfgFile.batch).to.equal(51);
            expect(cfgFinal.timeout).to.equal(cfgFile.timeout).to.equal(241);
            expect(cfgFinal.exclusions).to.equal(cfgFile.exclusions).to.equal("examples/dashLicensesExclusions.json");
            expect(cfgFinal.summary).to.equal(cfgFile.summary).to.equal("dependency-check-summary.txt");
        });
    });

    describe("Run dash-licenses with example yarn lock file", function() {
        // it("", function() {
        //     const yarnLockExample = path.resolve("..", "core", "src", "test", "java", "test_data_yarn.lock");
        //     const [status, output, error]  = runWrapper(["--inputfile", "yarnlockExample"]);
        //     console.log(output);
        //     expect(status).to.equal(0);
        // });
    });
});


// Utility functions: 

function extractConfigurationFromStdout(output, type, configParamName) {
    // console.log(`********* ${output} `)
    const match = output.match(configRegexps[type]);
    // const match = output.match(/^INFO: Effective configuration:(.*{.*})$/ms);
    // const config = match[1];
    const config = match? match[1] : undefined;
    // console.log(`^^^^^^^^^:${config}`);

    if (!config) {
        return;
    }

    let obj;
    try {
        obj = JSON.parse(config);
    } catch (e) {
        console.error("extractConfigurationFromStdout(): " + e);
    }

    if (config && configParamName) {
        return obj[`${configParamName}`];
    } else {
        return obj;
    }
}

/**
 * Returns the effective value of a given dash-licenses-wrapper parameter. i.e. the value that will
 * end-up being used once defaults, config file and CLI have been resolved. 
 * @param {string} output of dash-licenses-wrapper. The effective configuration is always printed except when using "--help"
 * @param {string} configParamName optional name of a parameter to extract from the effective configuration.
 * @returns {string | any | undefined} If configParamName is defined, return the corresponding value extracted from the tool's output. Otherwise, return the entire extracted effective configuration in the form of a JSON object.
 */
function extractEffectiveConfigurationFromStdout(output, configParamName) {
    // console.log(`********* ${output} `)
    const match = output.match(/^INFO: Effective configuration:(.*{.*})$/ms);
    const config = match[1];
    // console.log(`^^^^^^^^^:${config}`);

    let obj;
    try {
        obj = JSON.parse(config);
    } catch (e) {
        console.error("extractFileConfigurationFromStdout()" + e);
    }

    if (config && configParamName) {
        return obj[`${configParamName}`];
    } else {
        return obj;
    }

    // if (config && configParamName) {
    //     const match = config.match(new RegExp(`^\\s+(\\"${configParamName}.*?),?$`,'m'));
    //     const param = match[1];
    //     // console.log(`$$$$$ extracted: ${param}`);
    //     return param;
    // } else {
    //     return config;
    // }
}

/**
 * 
 * @param {string} output of dash-licenses-wrapper. Debug mode has to be enabled to see CLI configuration
 * @param {string} configParamName optional name of a parameter to extract from the CLI configuration. If omitted, the entire CLI configuration is returned as a JSON object.
 * @returns {string | any | undefined} If configParamName is defined, return the corresponding value extracted from the tool's output. Otherwise, return the entire extracted CLI configuration in the form of a JSON object. 
 */
function extractCLIConfigurationFromStdout(output, configParamName) {
    // console.log(`*********${output} `)
    const match = output.match(/^DEBUG: Parsed CLI:(.*?{.*?})$/ms);
    const config = match[1];
    // console.log(`^^^^^^^^^:${config}`);

    let obj;
    try {
        obj = JSON.parse(config);
    } catch (e) {
        console.error("extractCLIConfigurationFromStdout(): " + e);
    }

    if (config && configParamName) {
        return obj[`${configParamName}`];
    } else {
        return obj;
    }
}

/**
 * 
 * @param {string} output of dash-licenses-wrapper. Debug mode has to be enabled to see config file configuration
 * @param {string} configParamName optional: name of a config file parameter to extract from the configuration. 
 * @returns {string | any} If configParamName is defined, return the corresponding value extracted from the tool's output. Otherwise, return the entire extracted config file configuration in the form of a JSON object
 */
function extractFileConfigurationFromStdout(output, configParamName) {
    // console.log(`*********${output} `)
    const match = output.match(/^DEBUG: Parsed config file:(.*?{.*?})$/ms);
    const config = match[1];
    // console.log(`^^^^^^^^^:${config}`);

    let obj;
    try {
        obj = JSON.parse(config);
    } catch (e) {
        console.error("extractFileConfigurationFromStdout()" + e);
    }

    if (config && configParamName) {
        return obj[`${configParamName}`];
    } else {
        return obj;
    }
}