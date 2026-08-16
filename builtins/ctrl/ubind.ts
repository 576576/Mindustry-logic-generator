/**
 * ubind — 透传指令(规范见 docs/instructions/ctrl.md)
 */
namespace Builtins {
  export namespace Ctrl {
    export const ubind: InstrDef = {
      key: 'ubind',
      compile: function (s) { return 'ubind ' + s; },
      restore: function (s) { return 'ubind(' + s + ')'; }
    };
  }
}
