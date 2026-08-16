package cn.sumitm.mdtc.core;

/**
 * 内置指令处理器(实现位于 builtins/*.ts,经 BuiltinEngine 加载为可调用包装)。
 *
 * @param s   指令括号内(或指令字之后)trim 后的参数串
 * @param ctx 运行期桥接对象(cn.sumitm.mdtc.compiler.EmitCtx),提供编译状态与词法工具
 * @return    生成的 mdtcode 行(可含 \n 多行;空串表示已通过 ctx 写入 bash)
 */
@FunctionalInterface
public interface BuiltinHandler {
    String apply(String s, Object ctx);
}
