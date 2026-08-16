/**
 * format — 透传指令。
 *
 * 语法:`format(<值>)`
 * 输出:`format <s>`
 * 反编译:`format ` → `format(<s>)`
 */

namespace Builtins {
  export namespace Ctrl {
    export const format: InstrDef = {
      key: 'format',
      params: ['值'],
      compile: function (s) { return 'format ' + s; },
      restore: function (s) { return 'format(' + s + ')'; }
    };
  }
}
