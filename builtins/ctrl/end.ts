/**
 * end — 零参指令。
 *
 * 语法:`end()`
 * 输出:`end`(忽略参数)
 * 反编译:裸 `end` 行由 decompile 管道还原为 `end()`。
 */

namespace Builtins {
  export namespace Ctrl {
    export const end: InstrDef = {
      key: 'end',
      desc: '结束程序',
      compile: function () { return 'end'; }
    };
  }
}
