/**
 * stop — 零参指令。
 *
 * 语法:`stop()`
 * 输出:`stop`(忽略参数)
 * 反编译:裸 `stop` 行由 decompile 管道还原为 `stop()`。
 */

namespace Builtins {
  export namespace Ctrl {
    export const stop: InstrDef = {
      key: 'stop',
      compile: function () { return 'stop'; }
    };
  }
}
