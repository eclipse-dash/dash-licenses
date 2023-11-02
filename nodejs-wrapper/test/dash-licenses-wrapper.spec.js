// Mocha tests for dash-licenses-wrapper
const chai = require('chai');
const should = chai.should();
const expect = chai.expect;
// const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const wrapper = path.resolve("src", "dash-licenses-wrapper.js");

function runWrapper(args) {
    const command = `${wrapper}`;
    // const opts = { stdio: ['ignore', 'inherit', 'inherit']};
    const opts = { stdio: 'pipe'};
    let status = cp.spawnSync(command, args, opts);
    // console.log("***" + status.stdout);

    // "Debugger attached.\nWaiting for the debugger to disconnect...\n"
    // const error
    return [status.status, status.stdout.toString(), status.stderr.toString()];

}

describe("dash-licenses-wrapper tests", function() {
    describe("test CLI parameters", function() {
        it("test CLI param --help", function() {
            const [status, output, error]  = runWrapper(["--help"]);
            expect(status).to.equal(0);
            expect(error).to.be.empty;
            expect(output).to.match(/^.*Usage: dash-licenses-wrapper.js \[options\]/);
        });
        it("test output is in color by default", function() {
            const [status, output, error]  = runWrapper(["--help"]);
            expect(status).to.equal(0);
            expect(error).to.be.empty;
            expect(output).to.match(/^\x1b\[/);
        });
        it("test CLI param --noColor results in no color in output", function() {
            const [status, output, error]  = runWrapper(["--noColor",  "--help"]);
            expect(status).to.equal(0);
            expect(error).to.be.empty;
            expect(output).to.not.match(/^\x1b\[/);
        });
        it("test CLI param --dryRun results in exit before calling dash-licenses", function() {
            const [status, output, error]  = runWrapper(["--noColor",  "--dryRun"]);
            expect(status).to.equal(0);
            // dash-licenses outputs on stderr
            expect(error).to.be.empty;
            expect(output).to.match(/Dry-run mode enabled/);
        });
        it("test that dry run mode is disabled by default", function() {
            const [status, output, error]  = runWrapper(["--noColor", "--help"]);
            expect(status).to.equal(0);
            expect(error).to.be.empty;
            expect(output).to.not.match(/Dry-run mode enabled/);
        });
        it.only("test \"--debug\" CLI param enables extra traces", function() {
            const [status, output, error]  = runWrapper(["--debug", "--noColor", "--dryRun"]);
            // console.log("***" + output);
            // console.log("^^^" + error);
            expect(status).to.equal(0);
            // expect(error).to.equal("");
            
            expect(extractCLIConfiguration(output, "debug")).to.equal(true);
            const expectedCLIArgsDebugTrace = 
`DEBUG: Parsed CLI: {
  "debug": true,
  "noColor": true,
  "dryRun": true
}`;
            expect(output).to.match(new RegExp(`^${expectedCLIArgsDebugTrace}`,'gm'));
        });
        it("test CLI param --inputFile ", function() {
            const [status, output, error]  = runWrapper(["--noColor", "--dryRun"]);
            // console.log("***" + output);
            // console.log("^^^" + error);
            const entry = extractEffectiveConfiguration(output, "inputFile");
            // console.log(`****** ${entry}`);
            // expect(entry).to.equal("");
            // expect(error).to.be.empty;
            // expect(output).to.not.match(/Dry-run mode enabled/);
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

/**
 * Returns the effective value of a given dash-licenses-wrapper parameter. i.e. the value that will
 * end-up being used once defaults, config file and CLI have been resolved. 
 * @param {string} output of dash-licenses-wrapper. The effective configuration is always printed except when using "--help"
 * @param {string} configParamName optional name of a parameter to extract from the effective configuration.
 */
function extractEffectiveConfiguration(output, configParamName) {
    // console.log(`********* ${output} `)
    // const effectiveConfigTrace = output.match(/INFO: Effective configuration\:({.*})/gm);
    const match = output.match(/^INFO: Effective configuration:(.*{.*})$/ms);
    const config = match[1];
    // console.log(`^^^^^^^^^:${config}`);
    if (config && configParamName) {
        const match = config.match(new RegExp(`^\\s+(\\"${configParamName}.*?),?$`,'m'));
        const param = match[1];
        // console.log(`$$$$$ extracted: ${param}`);
        return param;
    } else {
        return config;
    }
}

/**
 * 
 * @param {string} output of dash-licenses-wrapper. Debug mode has to be enabled to see CLI configuration
 * @param {string} configParamName optional name of a parameter to extract from the CLI configuration. If omitted, the entire CLI configuration is returned as a JSON object.
 * @returns {string | any}
 */
function extractCLIConfiguration(output, configParamName) {
    // console.log(`*********${output} `)
    const match = output.match(/^DEBUG: Parsed CLI:(.*?{.*?})$/ms);
    const config = match[1];
    // console.log(`^^^^^^^^^:${config}`);

    let obj;
    try {
        obj = JSON.parse(config);
    } catch (e) {
        console.error(e);
    }

    if (config && configParamName) {
        return obj[`${configParamName}`];
    } else {
        return obj;
    }
}
