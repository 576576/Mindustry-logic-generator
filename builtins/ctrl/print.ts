/**
 * print — 透传指令。
 *
 * 语法:`print(<内容>)`
 * 输出:`print <s>`
 * 反编译:`print ` → `print(<s>)`
 */

namespace Builtins {
  export namespace Ctrl {
    export const print: InstrDef = {
      desc: '打印内容到消息栏',
      key: 'print',
      params: ['内容'],
      compile: function (s) { return 'print ' + s; },
      restore: function (s) { return 'print(' + s + ')'; }
    };
  }
}
