/**
 * jump2 — 计算跳转(规范见 docs/instructions/ctrl.md)
 * 无 restore:@counter 行折叠由 decompile 管道(convertJump2)处理。
 */
namespace Builtins {
  export namespace Ctrl {
    export const jump2: InstrDef = {
      key: 'jump2',
      compile: function (s, ctx) {
        const compiled = ctx.compileSub(ctx.split(s).length > 1 ? '@counter=@counter' + s : '@counter=' + s);
        ctx.bashAll(compiled.bash);
        return '';
      }
    };
  }
}
