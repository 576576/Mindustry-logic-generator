/**
 * wait — 透传指令。
 *
 * 语法:`wait(<秒数>)`
 * 输出:`wait <s>`
 * 反编译:`wait ` → `wait(<s>)`
 */

namespace Builtins {
  export namespace Ctrl {
    export const wait: InstrDef = {
      key: 'wait',
      compile: function (s) { return 'wait ' + s; },
      restore: function (s) { return 'wait(' + s + ')'; }
    };
  }
}
