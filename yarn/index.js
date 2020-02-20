/*************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
const fs = require('fs');
const lockfile = require('@yarnpkg/lockfile');
 
let file = fs.readFileSync('yarn.lock', 'utf8');
let json = lockfile.parse(file);

let all = new Set()
for (var package in json.object) {
    // The way to get the package name seems a little hacky,
    // but the lockfile API does not seem to provide an API
    // solution. Start looking for @ at index 1 so that packages
    // that start with @ work
    let end = package.substr(1).search('@')
    let dep = package.substr(0, end + 1) + '@' + json.object[package].version
    all.add(dep)
}
all.forEach(element => {
    console.log(element)
});
