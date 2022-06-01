%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }
@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [1 x i8*] [i8* bitcast (%_IntegerArray* (i8*,%_BooleanArray*)* @A.foo to i8*)]
define i32 @main() {
  %a = alloca i32
  %b = alloca i1
  %c = alloca %_IntegerArray*
  %d = alloca %_BooleanArray*
  %e = alloca i8*
  %_0 = sub i32 1, 2
  %_1 = add i32 %_0, 3
  %_2 = sub i32 %_1, 4
  %_3 = mul i32 %_2, 2
  store i32 %_3, i32* %a
  %_4 = load i32, i32* %a
  call void (i32) @print_int(%_4)
  store i1 1, i1* %b
  %_5 = icmp slt i32 10, 0
  br i1 %_5, label %arr_alloc6, label %arr_alloc7

%arr_alloc6:
  call void @throw_oob()
  br label %arr_alloc7

%arr_alloc7:
  %_8 = call i8* @calloc(i32 1,i32 12)
  %_9 = bitcast i8* %_8 to %_IntegerArray*
  %_10 = getelementptr %_IntegerArray, %_IntegerArray* %_9, i32 0, i32 0
  store i32 10, i32* %_10  %_11 = call i8* @calloc(i32 10, i32 4)
  %_12 = bitcast i8* %_11 to i32*
  %_13 = getelementptr %_IntegerArray, %_IntegerArray* %_9, i32 0, i32 1
  store i32* %_11, i32** %_12
  store %_IntegerArray* %_9, %_IntegerArray** %c
  %_14 = call i8* @calloc(i32 1, i32 16)
  %_15 = bitcast i8* %_14 to i8***
  %_16 = getelementptr [1 x i8*], [1 x i8*]* @.A_vtable, i32 0, i32 0
  store i8** %_16, i8*** %_15
  store i8* %_14, i8** %e
  %_17 = load i32, i32* %a
  %_18 = icmp slt i32* %_17, 0
  br i1 %_18, label %arr_alloc19, label %arr_alloc20

%arr_alloc19:
  call void @throw_oob()
  br label %arr_alloc20

%arr_alloc20:
  %_21 = call i8* @calloc(i32 1,i32 12)
  %_22 = bitcast i8* %_21 to %_BooleanArray*
  %_23 = getelementptr %_BooleanArray, %_BooleanArray* %_22, i32 0, i32 0
  store i32* %_17, i32* %_23
  %_24 = call i8* @calloc(i32* %_17, i32 1)
  %_25 = bitcast i8* %_24 to i1*
  %_26 = getelementptr %_BooleanArray, %_BooleanArray* %_22, i32 0, i32 1
  store i1* %_24, i1** %_25
  store %_BooleanArray* %_22, %_BooleanArray** %d
  ret i32 0
}
define %_IntegerArray* @A.foo(i8* %this, %_BooleanArray* %.a) {
  %a = alloca %_BooleanArray*
  store %_BooleanArray* %.a, %_BooleanArray** %a
  %b = alloca %_IntegerArray*
  %_0 = load %_BooleanArray*, %_BooleanArray** %a
  