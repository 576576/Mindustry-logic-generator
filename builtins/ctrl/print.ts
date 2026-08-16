/**
 * print — 透传指令(规范见 docs/instructions/ctrl.md)
 */
namespace Builtins {
  export namespace Ctrl {
    export const print: InstrDef = {
      key: 'print',
      compile: function (s) { return 'print ' + s; },
      restore: function (s) { return 'print(' + s + ')'; }
    };
  }
}
