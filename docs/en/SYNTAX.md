# MdtC Syntax Guide

> [← Back to README](README.md) · [Instruction Spec →](../instructions/README.md)

## File Types

| Extension | Purpose |
|-----------|---------|
| `.mdtc` | MdtC source — high-level logic code |
| `.mdtcode` | Mindustry logic assembly — compiled output |
| `.libmdtc` | Reusable function library |

---

## Comments & Tags

```
:: single-line comment / label
tag(global_label)
:: HEAD and END tags are auto-generated
:: DEFAULT tag marks the fallthrough entry
```

---

## Assignment & Arithmetic

```
x = 1 + 2
y = a - b            :: subtraction uses - with spaces around it (. - legacy also works)
z = -5                :: negative literals are fine

:: Supported operators
x = 1 + 2              :: add
x = a - b              :: sub
x = a * b              :: mul
x = a / b              :: div (float)
x = a // b             :: idiv (integer)
x = a .% b             :: mod
x = a %% b              :: emod
x = a .^ b             :: pow
x = a == b             :: equal
x = a != b             :: notEqual
x = a === b            :: strictEqual
x = a < b              :: lessThan
x = a <= b             :: lessThanEq
x = a > b              :: greaterThan
x = a >= b             :: greaterThanEq
x = a && b             :: land
x = a | b              :: or
x = a & b              :: and
x = a ^ b              :: xor
x = a << b             :: shl
x = a >> b             :: shr
x = a >>> b            :: ushr
```

---

## Math Functions

### Unary

```
not(x)   abs(x)   sign(x)
floor(x) ceil(x)  round(x)
sqrt(x)  rand(x)
sin(x)   cos(x)   tan(x)
asin(x)  acos(x)  atan(x)
ln(x)    lg(x)    lb(x)
```

### Binary

```
max(a, b)       min(a, b)
len(a, b)       angle(a, b)
angleDiff(a, b) noise(a, b)
log(base, x)
```

---

## Control Statements (Side-Effect Free)

```
print("text")        :: print to message block
printchar(x)         :: print character by code point
format(x)            :: append formatted value
printf("str", a, b)  :: print + format chain
wait(seconds)        :: pause execution
stop()               :: stop processor
end()                :: jump to start
```

---

## Unit Control

```
ubind(@unit_type)
uctrl(type, x, y, angle, velocity)
ushoot(x, y, shoot).target(target)
:: target is optional — defaults to @this
:: .shooting() chain sets whether to fire
```

---

## Block Control (Dot-chain)

```
:: Block reference as variable
reactor = link(id)

:: Control
reactor.enable(0|1)
reactor.config(value)
reactor.color(r, g, b, a)

:: Shooting — shoot() takes only the fire switch
:: target: two args (x,y) = coordinates, one arg = design target
turret.shoot(shoot).target(x, y)

:: Locate
result = core.ulocate(type).ore(ore).building(bld).enemy(enemy)

:: Read / sense
result = core.sensor(@block_type)
result = cell1.read(cell_number)
```

### Supported `.ulocate` Types

`ore`, `building` — plus building sub-types: `core`, `storage`, `generator`, `turret`, `factory`, `repair`, `battery`, `reactor`, `drill`, `shield`

---

## Data Retrieval

```
value = link(link_index)               :: getlink
value = block(type, index)             :: lookup block
value = unit(type, index)              :: lookup unit
value = item(type, index)              :: lookup item
value = liquid(type, index)            :: lookup liquid
value = team(type, index)              :: lookup team
value = lookup(type, index)            :: generic lookup
color = pack(r, g, b, a)              :: packcolor
result = block.sensor(@property)       :: sensor
result = block.read(cell_number)       :: read
result = expr.sensor(@prop)            :: dot-chain sensor
result = expr.read(cell)               :: dot-chain read
result = expr.orElse(fallback).when(cond)  :: conditional select
```

---

## Flow Control

### If / Else

```
if(condition){
    :: true branch
}
else{
    :: false branch
}
```

### Unconditional Jump

```
jump(target_label)
:: when(cond) adds a condition
jump(target_label, when(x > 0))
jump(target_label, when(always))
jump(target_label, when(never))
```

### Computed Jump

```
index = expr
jump2(index)
:: @counter is auto-assigned
```

### do-while Loop

```
do{
    :: loop body
    :: use jump to exit
}
```

### for Loop

```
for(var, link_list){
    :: iterate over linked blocks
    :: var holds current @links index
}
```

---

## Functions

```
:: Void function
function println(str){
    print(str)
    printchar(10)
}

:: Returning function
function isReactorSafe(reactor){
    heat = reactor.sensor(@heat)
    thorium = reactor.sensor(@thorium)
    return heat < 0.5 && thorium < 5
}

:: Call
result = isReactorSafe(nuke)
println("Hello")
```

---

## Import & Repeat

```
:: Import function library
import modules/example

:: Repeat macro — unrolled at compile time
repeat(flag, 3){
    ubind(@poly)
    flag.move(20, 0)
}
:: Expands: flag1.move(...) flag2.move(...) flag3.move(...)
```

---

## Raw Escape

```
raw("raw mindustry logic instruction here")
:: Pass-through to output with no compilation
```

---

## Examples

See [`sample_cases/`](../../sample_cases/) for complete working examples:

| File | Description |
|------|-------------|
| `case1.mdtc` | All operators & math functions |
| `case2.mdtc` | Control statements |
| `case3.mdtc` | Block/unit/item/liquid/team lookup |
| `case4.mdtc` | Flow control (if/for/do/jump) |
| `case5.mdtc` | Function definitions |
| `case6.mdtc` | Import & repeat |
| `failsafe_钍堆.mdtc` | Thorium reactor safety |
| `mine u5.mdtc` | 5-unit mining swarm |
| `智能装卸.mdtc` | Smart unloader (2000+ lines) |
