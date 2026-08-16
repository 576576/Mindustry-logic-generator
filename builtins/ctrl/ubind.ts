/**
 * ubind — 透传指令。
 *
 * 语法:`ubind(<单位类型>)`
 * 输出:`ubind <s>`
 * 反编译:`ubind ` → `ubind(<s>)`
 */

namespace Builtins {
  export namespace Ctrl {
    export const ubind: InstrDef = {
      key: 'ubind',
      params: ['单位类型'],
      compile: function (s) { return 'ubind ' + s; },
      restore: function (s) { return 'ubind(' + s + ')'; }
    };
  }
}
