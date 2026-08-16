/**
 * jump2 — 计算跳转;不直接产出行,无 restore(多行折叠由 decompile 管道处理)。
 *
 * 语法:`jump2(<表达式或增量表达式>)`
 * 输出:将 `s` 转为 `@counter=<s>`(单 token)或 `@counter=@counter<s>`(多 token),
 *   交给子编译后把产物行写入 bash 列表,返回空串。
 * 反编译:decompile 管道 convertJump2 把 `@counter=` 行折叠为 `jump2(...)`。
 */

namespace Builtins {
  export namespace Ctrl {
    export const jump2: InstrDef = {
      key: 'jump2',
      params: ['表达式或增量表达式'],
      compile: function (s, ctx) {
        const compiled = ctx.compileSub(ctx.split(s).length > 1 ? '@counter=@counter' + s : '@counter=' + s);
        ctx.bashAll(compiled.bash);
        return '';
      }
    };
  }
}
