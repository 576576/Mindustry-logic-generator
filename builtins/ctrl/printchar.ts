/**
 * printchar — 透传指令。
 *
 * 语法:`printchar(<码点>)`
 * 输出:`printchar <s>`
 * 反编译:`printchar ` → `printchar(<s>)`
 */

namespace Builtins {
  export namespace Ctrl {
    export const printchar: InstrDef = {
      key: 'printchar',
      compile: function (s) { return 'printchar ' + s; },
      restore: function (s) { return 'printchar(' + s + ')'; }
    };
  }
}
