# fov-demo

A simple Field of View viewer to demonstrate several strategies and compare them side-by-side.
The left view will cycle through various strategies found in the `squidlib` library. https://github.com/SquidPony/SquidLib
The right view will always show the strategy found in the `heckendorf` game. https://github.com/uosl/heckendorf

## Installation

Clone this repository locally.

## Usage

From the terminal:
```
lein run
```

From within a REPL:
```
(future
  (run :swing))
```

## Keyboard controls

### Movement

`asdf`, `hjkl`, or arrow keys

### Other

`r` - Regenerate dungeon
`t` - Cycle through FOV types
`q` - Quit

## Screen

If the screen gets resized, the world will be regenerated with the new size.





Copyright © 2019 Steve Ashton

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
