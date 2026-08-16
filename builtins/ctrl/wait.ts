/**
 * wait — 透传指令(规范见 docs/instructions/ctrl.md)
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
