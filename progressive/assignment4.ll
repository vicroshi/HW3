%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }
@.Main_vtable = global [0 x i8*] []
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
  %_0 = sub i32 1, 2
  %_1 = add i32 %_0, 3
  %_2 = sub i32 %_1, 4
  %_3 = mul i32 %_2, 2
  store i32 %_3, i32* %a
  %_4 = load i32, i32* %a
  call void (i32) @print_int(i32 %_4)
  store i1 1, i1* %b
  %_5 = icmp slt i32 10, 0
  br i1 %_5, label %arr_alloc6, label %arr_alloc7

arr_alloc6:
  call void @throw_oob()
  br label %arr_alloc7

arr_alloc7:
  %_8 = call i8* @calloc(i32 1,i32 12)
  %_9 = bitcast i8* %_8 to %_IntegerArray*
  %_10 = getelementptr %_IntegerArray, %_IntegerArray* %_9, i32 0, i32 0
  store i32 10, i32* %_10
  %_11 = call i8* @calloc(i32 10, i32 4)
  %_12 = bitcast i8* %_11 to i32*
  %_13 = getelementptr %_IntegerArray, %_IntegerArray* %_9, i32 0, i32 1
  store i32* %_12, i32** %_13
  store %_IntegerArray* %_9, %_IntegerArray** %c
  %_14 = load i32, i32* %a
  %_15 = add i32 %_14, 10
  %_16 = icmp slt i32 %_15, 0
  br i1 %_16, label %arr_alloc17, label %arr_alloc18

arr_alloc17:
  call void @throw_oob()
  br label %arr_alloc18

arr_alloc18:
  %_19 = call i8* @calloc(i32 1,i32 12)
  %_20 = bitcast i8* %_19 to %_BooleanArray*
  %_21 = getelementptr %_BooleanArray, %_BooleanArray* %_20, i32 0, i32 0
  store i32 %_15, i32* %_21
  %_22 = call i8* @calloc(i32 %_15, i32 1)
  %_23 = bitcast i8* %_22 to i1*
  %_24 = getelementptr %_BooleanArray, %_BooleanArray* %_20, i32 0, i32 1
  store i1* %_23, i1** %_24
  store %_BooleanArray* %_20, %_BooleanArray** %d
  %_25 = load %_IntegerArray*, %_IntegerArray** %c
  %_26 = getelementptr %_IntegerArray, %_IntegerArray* %_25, i32 0, i32 0
  %_27 = load i32, i32* %_26
  %_28 = icmp slt i32 1, %_27
  %_29 = icmp sge i32 1, 0
  %_30 = and i1 %_29, %_28
  br i1 %_30, label %arr_look32, label %out_of_bounds31

out_of_bounds31:
  call void @throw_oob()
  br label %arr_look32

arr_look32:
  %_33 = getelementptr %_IntegerArray, %_IntegerArray* %_25, i32 0, i32 1
  %_34 = load i32* , i32** %_33
  %_35 = getelementptr i32, i32* %_34, i32 1
  store i32 6, i32* %_35
  %_36 = load %_IntegerArray*, %_IntegerArray** %c
  %_37 = getelementptr %_IntegerArray, %_IntegerArray* %_36, i32 0, i32 0
  %_38 = load i32, i32* %_37
  %_40 = load i32, i32* %a
  %_41 = add i32 %_40, 11
  %_39 = icmp slt i32 %_41, %_38
  %_42 = icmp sge i32 %_41, 0
  %_43 = and i1 %_42, %_39
  br i1 %_43, label %arr_look45, label %out_of_bounds44

out_of_bounds44:
  call void @throw_oob()
  br label %arr_look45

arr_look45:
  %_46 = getelementptr %_IntegerArray, %_IntegerArray* %_36, i32 0, i32 1
  %_47 = load i32* , i32** %_46
  %_48 = getelementptr i32, i32* %_47, i32 %_41
  %_49 = load %_IntegerArray*, %_IntegerArray** %c
  %_50 = getelementptr %_IntegerArray, %_IntegerArray* %_49, i32 0, i32 0
  %_51 = load i32, i32* %_50
  %_52 = icmp slt i32 1, %_51
  %_53 = icmp sge i32 1, 0
  %_54 = and i1 %_53, %_52

  br i1 %_54, label %arr_look56, label %out_of_bounds55

out_of_bounds55:
  call void @throw_oob()
  br label %arr_look56

arr_look56:
  %_57 = getelementptr %_IntegerArray, %_IntegerArray* %_49, i32 0, i32 1
  %_58 = load i32* , i32** %_57
  %_59 = getelementptr i32, i32* %_58, i32 1
  %_60 = load i32, i32* %_59
  store i32 %_60, i32* %_48
  %_61 = load %_IntegerArray*, %_IntegerArray** %c
  %_62 = getelementptr %_IntegerArray, %_IntegerArray* %_61, i32 0, i32 0
  %_63 = load i32, i32* %_62
  %_65 = load %_IntegerArray*, %_IntegerArray** %c
  %_66 = getelementptr %_IntegerArray, %_IntegerArray* %_65, i32 0, i32 0
  %_67 = load i32, i32* %_66
  %_69 = load i32, i32* %a
  %_70 = add i32 %_69, 11
  %_68 = icmp slt i32 %_70, %_67
  %_71 = icmp sge i32 %_70, 0
  %_72 = and i1 %_71, %_68

  br i1 %_72, label %arr_look74, label %out_of_bounds73

out_of_bounds73:
  call void @throw_oob()
  br label %arr_look74

arr_look74:
  %_75 = getelementptr %_IntegerArray, %_IntegerArray* %_65, i32 0, i32 1
  %_76 = load i32* , i32** %_75
  %_77 = getelementptr i32, i32* %_76, i32 %_70
  %_78 = load i32, i32* %_77
  %_64 = icmp slt i32 %_78, %_63
  %_79 = icmp sge i32 %_78, 0
  %_80 = and i1 %_79, %_64
  br i1 %_80, label %arr_look82, label %out_of_bounds81

out_of_bounds81:
  call void @throw_oob()
  br label %arr_look82

arr_look82:
  %_83 = getelementptr %_IntegerArray, %_IntegerArray* %_61, i32 0, i32 1
  %_84 = load i32* , i32** %_83
  %_85 = getelementptr i32, i32* %_84, i32 %_78
  store i32 101, i32* %_85
  %_86 = load %_IntegerArray*, %_IntegerArray** %c
  %_87 = getelementptr %_IntegerArray, %_IntegerArray* %_86, i32 0, i32 0
  %_88 = load i32, i32* %_87
  %_89 = icmp slt i32 1, %_88
  %_90 = icmp sge i32 1, 0
  %_91 = and i1 %_90, %_89

  br i1 %_91, label %arr_look93, label %out_of_bounds92

out_of_bounds92:
  call void @throw_oob()
  br label %arr_look93

arr_look93:
  %_94 = getelementptr %_IntegerArray, %_IntegerArray* %_86, i32 0, i32 1
  %_95 = load i32* , i32** %_94
  %_96 = getelementptr i32, i32* %_95, i32 1
  %_97 = load i32, i32* %_96
  call void (i32) @print_int(i32 %_97)
  %_98 = load %_IntegerArray*, %_IntegerArray** %c
  %_99 = getelementptr %_IntegerArray, %_IntegerArray* %_98, i32 0, i32 0
  %_100 = load i32, i32* %_99
  %_101 = icmp slt i32 6, %_100
  %_102 = icmp sge i32 6, 0
  %_103 = and i1 %_102, %_101

  br i1 %_103, label %arr_look105, label %out_of_bounds104

out_of_bounds104:
  call void @throw_oob()
  br label %arr_look105

arr_look105:
  %_106 = getelementptr %_IntegerArray, %_IntegerArray* %_98, i32 0, i32 1
  %_107 = load i32* , i32** %_106
  %_108 = getelementptr i32, i32* %_107, i32 6
  %_109 = load i32, i32* %_108
  call void (i32) @print_int(i32 %_109)
  %_110 = load %_IntegerArray*, %_IntegerArray** %c
  %_111 = getelementptr %_IntegerArray, %_IntegerArray* %_110, i32 0, i32 0
  %_112 = load i32, i32* %_111
  %_113 = icmp slt i32 7, %_112
  %_114 = icmp sge i32 7, 0
  %_115 = and i1 %_114, %_113

  br i1 %_115, label %arr_look117, label %out_of_bounds116

out_of_bounds116:
  call void @throw_oob()
  br label %arr_look117

arr_look117:
  %_118 = getelementptr %_IntegerArray, %_IntegerArray* %_110, i32 0, i32 1
  %_119 = load i32* , i32** %_118
  %_120 = getelementptr i32, i32* %_119, i32 7
  %_121 = load i32, i32* %_120
  call void (i32) @print_int(i32 %_121)
  ret i32 0
}
