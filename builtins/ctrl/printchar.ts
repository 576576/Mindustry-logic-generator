/**
 * printchar — 透传指令(规范见 docs/instructions/ctrl.md)
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
