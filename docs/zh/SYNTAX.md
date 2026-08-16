# MdtC 语法指南

> [← 返回 README](README.md) · [内置指令规范 →](../instructions/README.md)

## 文件类型

| 扩展名 | 用途 |
|-----------|---------|
| `.mdtc` | MdtC 源码 — 高级逻辑代码 |
| `.mdtcode` | Mindustry 逻辑汇编 — 编译输出 |
| `.libmdtc` | 可复用函数库 |

---

## 注释与标签

```
:: 单行注释 / 标签
tag(全局标签)
:: HEAD 和 END 标签自动生成
:: DEFAULT 标签标记入口
```

---

## 赋值与运算

```
x = 1 + 2
y = a - b            :: 减法用 -（前后空格；旧写法 .- 仍兼容）
z = -5                :: 负数直接书写

:: 支持的运算符
x = 1 + 2              :: add 加
x = a - b              :: sub 减
x = a * b              :: mul 乘
x = a / b              :: div 除（浮点）
x = a // b             :: idiv 整除
x = a .% b             :: mod 取模
x = a %% b              :: emod 浮点取模
x = a .^ b             :: pow 幂
x = a == b             :: equal 等于
x = a != b             :: notEqual 不等于
x = a === b            :: strictEqual 严格等于
x = a < b              :: lessThan 小于
x = a <= b             :: lessThanEq 小于等于
x = a > b              :: greaterThan 大于
x = a >= b             :: greaterThanEq 大于等于
x = a && b             :: land 逻辑与
x = a | b              :: or 或
x = a & b              :: and 位与
x = a ^ b              :: xor 异或
x = a << b             :: shl 左移
x = a >> b             :: shr 右移
x = a >>> b            :: ushr 无符号右移
```

---

## 数学函数

### 一元

```
not(x)   abs(x)   sign(x)
floor(x) ceil(x)  round(x)
sqrt(x)  rand(x)
sin(x)   cos(x)   tan(x)
asin(x)  acos(x)  atan(x)
ln(x)    lg(x)    lb(x)
```

### 二元

```
max(a, b)       min(a, b)
len(a, b)       angle(a, b)
angleDiff(a, b) noise(a, b)
log(base, x)
```

---

## 控制语句（无副作用）

```
print("文本")         :: 打印到消息块
printchar(x)          :: 按码点打印字符
format(x)             :: 追加格式化值
printf("字符串", a, b) :: 打印 + 格式化链
wait(秒数)            :: 暂停执行
stop()                :: 停止处理器
end()                 :: 跳转到开头
```

---

## 单位控制

```
ubind(@单位类型)
uctrl(类型, x, y, 角度, 速度)
ushoot(x, y, 射击开关).target(目标)
:: target 可选 — 默认 @this
:: .shooting() 链控制是否开火
```

---

## 方块控制（点链）

```
:: 方块引用赋值
reactor = link(id)

:: 控制
reactor.enable(0|1)
reactor.config(value)
reactor.color(r, g, b, a)

:: 射击 — shoot() 只带射击开关
:: target:双参 (x,y) 为坐标,单参为设计目标
turret.shoot(shoot).target(x, y)

:: 定位
result = core.ulocate(类型).ore(矿物).building(建筑).enemy(敌方)

:: 读取/感知
result = core.sensor(@方块类型)
result = cell1.read(单元号)
```

### 支持的 `.ulocate` 类型

`ore`（矿物）, `building`（建筑）— 建筑子类型：`core`, `storage`, `generator`, `turret`, `factory`, `repair`, `battery`, `reactor`, `drill`, `shield`

---

## 数据获取

```
value = link(链接索引)                :: getlink
value = block(类型, 索引)             :: lookup 方块
value = unit(类型, 索引)              :: lookup 单位
value = item(类型, 索引)              :: lookup 物品
value = liquid(类型, 索引)            :: lookup 液体
value = team(类型, 索引)              :: lookup 队伍
value = lookup(类型, 索引)            :: 通用 lookup
color = pack(r, g, b, a)             :: packcolor
result = block.sensor(@属性)          :: sensor
result = block.read(单元号)           :: read
result = expr.sensor(@属性)            :: 点链 sensor
result = expr.read(单元号)           :: 点链 read
result = expr.orElse(备用值).when(条件) :: 条件选择
```

---

## 流程控制

### If / Else

```
if(条件){
    :: 真分支
}
else{
    :: 假分支
}
```

### 无条件跳转

```
jump(目标标签)
:: when(条件) 添加条件跳转
jump(目标标签, when(x > 0))
jump(目标标签, when(always))
jump(目标标签, when(never))
```

### 计算跳转

```
index = 表达式
jump2(index)
:: @counter 自动赋值
```

### do-while 循环

```
do{
    :: 循环体
    :: 用 jump 退出
}
```

### for 循环

```
for(变量, 链接列表){
    :: 遍历已链接方块
    :: 变量保存当前 @links 索引
}
```

---

## 函数

```
:: 无返回值函数
function println(str){
    print(str)
    printchar(10)
}

:: 有返回值函数
function isReactorSafe(reactor){
    heat = reactor.sensor(@heat)
    thorium = reactor.sensor(@thorium)
    return heat < 0.5 && thorium < 5
}

:: 调用
result = isReactorSafe(nuke)
println("Hello")
```

---

## 导入与 Repeat

```
:: 导入函数库
import modules/example

:: repeat 宏 — 编译时展开
repeat(flag, 3){
    ubind(@poly)
    flag.move(20, 0)
}
:: 展开为: flag1.move(...) flag2.move(...) flag3.move(...)
```

---

## Raw 原始转义

```
raw("这里是原始 Mindustry 逻辑指令")
:: 直接透传到输出，不做编译
```

---

## 示例

完整可运行样例见 [`sample_cases/`](../../sample_cases/)：

| 文件 | 说明 |
|------|-------------|
| `case1.mdtc` | 所有运算符与数学函数 |
| `case2.mdtc` | 控制语句 |
| `case3.mdtc` | 方块/单位/物品/液体/队伍查找 |
| `case4.mdtc` | 流程控制（if/for/do/jump） |
| `case5.mdtc` | 函数定义 |
| `case6.mdtc` | 导入与 repeat |
| `failsafe_钍堆.mdtc` | 钍反应堆安全保护 |
| `mine u5.mdtc` | 5 单位采矿集群 |
| `智能装卸.mdtc` | 智能装卸器（2000+ 行） |
