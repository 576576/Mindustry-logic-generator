# {{syntax.title}}

> [{{syntax.back_readme}}](README.md) · [{{syntax.back_spec}}](../instructions/README.md)

## {{syntax.h_files}}

| {{syntax.ft_col1}} | {{syntax.ft_col2}} |
|-----------|---------|
| `.mdtc` | {{syntax.ft_mdtc}} |
| `.mdtcode` | {{syntax.ft_mdtcode}} |
| `.libmdtc` | {{syntax.ft_lib}} |

---

## {{syntax.h_comments}}

```
:: {{syntax.c_comment}}
{{syntax.c_tag}}
:: {{syntax.c_head}}
:: {{syntax.c_default}}
```

---

## {{syntax.h_assign}}

```
x = 1 + 2
y = a - b            :: {{syntax.a_sub}}
z = -5                :: {{syntax.a_neg}}

:: {{syntax.a_ops}}
x = 1 + 2              :: {{syntax.op_add}}
x = a - b              :: {{syntax.op_sub}}
x = a * b              :: {{syntax.op_mul}}
x = a / b              :: {{syntax.op_div}}
x = a // b             :: {{syntax.op_idiv}}
x = a .% b             :: {{syntax.op_mod}}
x = a %% b              :: {{syntax.op_emod}}
x = a .^ b             :: {{syntax.op_pow}}
x = a == b             :: {{syntax.op_equal}}
x = a != b             :: {{syntax.op_notEqual}}
x = a === b            :: {{syntax.op_strictEqual}}
x = a < b              :: {{syntax.op_lessThan}}
x = a <= b             :: {{syntax.op_lessThanEq}}
x = a > b              :: {{syntax.op_greaterThan}}
x = a >= b             :: {{syntax.op_greaterThanEq}}
x = a && b             :: {{syntax.op_land}}
x = a | b              :: {{syntax.op_or}}
x = a & b              :: {{syntax.op_and}}
x = a ^ b              :: {{syntax.op_xor}}
x = a << b             :: {{syntax.op_shl}}
x = a >> b             :: {{syntax.op_shr}}
x = a >>> b            :: {{syntax.op_ushr}}
```

---

## {{syntax.h_math}}

### {{syntax.h_unary}}

```
not(x)   abs(x)   sign(x)
floor(x) ceil(x)  round(x)
sqrt(x)  rand(x)
sin(x)   cos(x)   tan(x)
asin(x)  acos(x)  atan(x)
ln(x)    lg(x)    lb(x)
```

### {{syntax.h_binary}}

```
max(a, b)       min(a, b)
len(a, b)       angle(a, b)
angleDiff(a, b) noise(a, b)
log(base, x)
```

---

## {{syntax.h_ctrl}}

```
{{syntax.ct_print}}
{{syntax.ct_printchar}}
{{syntax.ct_format}}
{{syntax.ct_printf}}
{{syntax.ct_wait}}
{{syntax.ct_stop}}
{{syntax.ct_end}}
```

---

## {{syntax.h_unit}}

```
{{syntax.u_bind}}
{{syntax.u_ctrl}}
{{syntax.u_ushoot}}
:: {{syntax.u_target}}
:: {{syntax.u_shooting}}
```

---

## {{syntax.h_block}}

```
:: {{syntax.b_var}}
reactor = link(id)

:: {{syntax.b_ctrl}}
reactor.enable(0|1)
reactor.config(value)
reactor.color(r, g, b, a)

:: {{syntax.b_shoot}}
:: {{syntax.b_target}}
turret.shoot(shoot).target(x, y)

:: {{syntax.b_locate}}
{{syntax.b_ulocate}}

:: {{syntax.b_read}}
{{syntax.b_sensor}}
{{syntax.b_readline}}
```

### {{syntax.h_ulocate}}

{{syntax.ulocate_desc}}

---

## {{syntax.h_data}}

```
{{syntax.d_link}}
{{syntax.d_block}}
{{syntax.d_unit}}
{{syntax.d_item}}
{{syntax.d_liquid}}
{{syntax.d_team}}
{{syntax.d_lookup}}
{{syntax.d_pack}}
{{syntax.d_sensor}}
{{syntax.d_read}}
{{syntax.d_chain_sensor}}
{{syntax.d_chain_read}}
{{syntax.d_orElse}}
```

---

## {{syntax.h_flow}}

### {{syntax.h_if}}

```
{{syntax.f_if_open}}
    :: {{syntax.f_true}}
}
else{
    :: {{syntax.f_false}}
}
```

### {{syntax.h_jump}}

```
{{syntax.f_jump_a}}
:: {{syntax.f_when}}
{{syntax.f_jump_b}}
{{syntax.f_jump_c}}
{{syntax.f_jump_d}}
```

### {{syntax.h_jump2}}

```
{{syntax.f_jump2_assign}}
jump2(index)
:: {{syntax.f_counter}}
```

### {{syntax.h_do}}

```
do{
    :: {{syntax.f_body}}
    :: {{syntax.f_exit}}
}
```

### {{syntax.h_for}}

```
{{syntax.f_for_open}}
    :: {{syntax.f_iterate}}
    :: {{syntax.f_index}}
}
```

---

## {{syntax.h_func}}

```
:: {{syntax.fn_void}}
function println(str){
    print(str)
    printchar(10)
}

:: {{syntax.fn_return}}
function isReactorSafe(reactor){
    heat = reactor.sensor(@heat)
    thorium = reactor.sensor(@thorium)
    return heat < 0.5 && thorium < 5
}

:: {{syntax.fn_call}}
result = isReactorSafe(nuke)
println("Hello")
```

---

## {{syntax.h_import}}

```
:: {{syntax.i_import}}
import modules/example

:: {{syntax.i_repeat}}
repeat(flag, 3){
    ubind(@poly)
    flag.move(20, 0)
}
:: {{syntax.i_expand}}
```

---

## {{syntax.h_raw}}

```
{{syntax.r_raw}}
:: {{syntax.r_pass}}
```

---

## {{syntax.h_examples}}

{{syntax.ex_intro}}

| {{syntax.ex_col1}} | {{syntax.ex_col2}} |
|------|-------------|
| `case1.mdtc` | {{syntax.ex_case1}} |
| `case2.mdtc` | {{syntax.ex_case2}} |
| `case3.mdtc` | {{syntax.ex_case3}} |
| `case4.mdtc` | {{syntax.ex_case4}} |
| `case5.mdtc` | {{syntax.ex_case5}} |
| `case6.mdtc` | {{syntax.ex_case6}} |
| `failsafe_钍堆.mdtc` | {{syntax.ex_failsafe}} |
| `mine u5.mdtc` | {{syntax.ex_mine}} |
| `智能装卸.mdtc` | {{syntax.ex_smart}} |
