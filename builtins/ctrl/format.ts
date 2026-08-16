/**
 * format — 透传指令(规范见 docs/instructions/ctrl.md)
 */
namespace Builtins {
  export namespace Ctrl {
    export const format: InstrDef = {
      key: 'format',
      compile: function (s) { return 'format ' + s; },
      restore: function (s) { return 'format(' + s + ')'; }
    };
  }
}
