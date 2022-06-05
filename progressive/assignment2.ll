%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }
@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [0 x i8*] []
declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"

define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
  %a = alloca i32
  %b = alloca i1
  %c = alloca %_IntegerArray*
  %d = alloca %_BooleanArray*
  %e = alloca i8*
  %_0 = sub i32 0, 1
  store i32 %_0, i32* %a
  store i1 1, i1* %b
  %_1 = icmp slt i32 10, 0
  br i1 %_1, label %arr_alloc2, label %arr_alloc3

arr_alloc2:
  call void @throw_oob()
  br label %arr_alloc3

arr_alloc3:
  %_4 = call i8* @calloc(i32 1,i32 12)
  %_5 = bitcast i8* %_4 to %_IntegerArray*
  %_6 = getelementptr %_IntegerArray, %_IntegerArray* %_5, i32 0, i32 0
  store i32 10, i32* %_6
  %_7 = call i8* @calloc(i32 10, i32 4)
  %_8 = bitcast i8* %_7 to i32*
  %_9 = getelementptr %_IntegerArray, %_IntegerArray* %_5, i32 0, i32 1
  store i32* %_8, i32** %_9
  store %_IntegerArray* %_5, %_IntegerArray** %c
  %_10 = icmp slt i32 11, 0
  br i1 %_10, label %arr_alloc11, label %arr_alloc12

arr_alloc11:
  call void @throw_oob()
  br label %arr_alloc12

arr_alloc12:
  %_13 = call i8* @calloc(i32 1,i32 12)
  %_14 = bitcast i8* %_13 to %_BooleanArray*
  %_15 = getelementptr %_BooleanArray, %_BooleanArray* %_14, i32 0, i32 0
  store i32 11, i32* %_15
  %_16 = call i8* @calloc(i32 11, i32 1)
  %_17 = bitcast i8* %_16 to i1*
  %_18 = getelementptr %_BooleanArray, %_BooleanArray* %_14, i32 0, i32 1
  store i1* %_17, i1** %_18
  store %_BooleanArray* %_14, %_BooleanArray** %d
  %_19 = call i8* @calloc(i32 1, i32 8)
  %_20 = bitcast i8* %_19 to i8***
  %_21 = getelementptr [0 x i8*], [0 x i8*]* @.A_vtable, i32 0, i32 0
  store i8** %_21, i8*** %_20
  store i8* %_19, i8** %e
  ret i32 0
}
