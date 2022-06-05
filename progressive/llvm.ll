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
  %a = alloca %_IntegerArray*
  %i = alloca i32
  %_0 = icmp slt i32 10, 0
  br i1 %_0, label %arr_alloc1, label %arr_alloc2

arr_alloc1:
  call void @throw_oob()
  br label %arr_alloc2

arr_alloc2:
  %_3 = call i8* @calloc(i32 1,i32 12)
  %_4 = bitcast i8* %_3 to %_IntegerArray*
  %_5 = getelementptr %_IntegerArray, %_IntegerArray* %_4, i32 0, i32 0
  store i32 10, i32* %_5
  %_6 = call i8* @calloc(i32 10, i32 4)
  %_7 = bitcast i8* %_6 to i32*
  %_8 = getelementptr %_IntegerArray, %_IntegerArray* %_4, i32 0, i32 1
  store i32* %_7, i32** %_8
  store %_IntegerArray* %_4, %_IntegerArray** %a
  store i32 0, i32* %i
  br label %while9

while9:
  %_12 = load i32, i32* %i
  %_13 = load %_IntegerArray*, %_IntegerArray** %a
  %_14 = getelementptr %_IntegerArray, %_IntegerArray* %_13, i32 0, i32 0
  %_15 = load i32, i32* %_14
  %_16 = icmp slt i32 %_12, %_15
  br i1 %_16, label %body10, label %exit_while11

body10:
  %_17 = load %_IntegerArray*, %_IntegerArray** %a
  %_18 = getelementptr %_IntegerArray, %_IntegerArray* %_17, i32 0, i32 0
  %_19 = load i32, i32* %_18
  %_21 = load i32, i32* %i
  %_20 = icmp slt i32 %_21, %_19
  %_22 = icmp sge i32 %_21, 0
  %_23 = and i1 %_22, %_20
  br i1 %_23, label %arr_look25, label %out_of_bounds24

out_of_bounds24:
  call void @throw_oob()
  br label %arr_look25

arr_look25:
  %_26 = getelementptr %_IntegerArray, %_IntegerArray* %_17, i32 0, i32 1
  %_27 = load i32* , i32** %_26
  %_28 = getelementptr i32, i32* %_27, i32 %_21
  %_29 = load i32, i32* %i
  store i32 %_29, i32* %_28
  %_30 = load i32, i32* %i
  %_31 = add i32 %_30, 1
  store i32 %_31, i32* %i
  br label %while9
exit_while11:
  store i32 0, i32* %i
  br label %while32

while32:
  %_35 = load i32, i32* %i
  %_36 = load %_IntegerArray*, %_IntegerArray** %a
  %_37 = getelementptr %_IntegerArray, %_IntegerArray* %_36, i32 0, i32 0
  %_38 = load i32, i32* %_37
  %_39 = icmp slt i32 %_35, %_38
  br i1 %_39, label %body33, label %exit_while34

body33:
  %_40 = load %_IntegerArray*, %_IntegerArray** %a
  %_41 = getelementptr %_IntegerArray, %_IntegerArray* %_40, i32 0, i32 0
  %_42 = load i32, i32* %_41
  %_44 = load i32, i32* %i
  %_43 = icmp slt i32 %_44, %_42
  %_45 = icmp sge i32 %_44, 0
  %_46 = and i1 %_45, %_43

  br i1 %_46, label %arr_look48, label %out_of_bounds47

out_of_bounds47:
  call void @throw_oob()
  br label %arr_look48

arr_look48:
  %_49 = getelementptr %_IntegerArray, %_IntegerArray* %_40, i32 0, i32 1
  %_50 = load i32* , i32** %_49
  %_51 = getelementptr i32, i32* %_50, i32 %_44
  %_52 = load i32, i32* %_51
  call void (i32) @print_int(i32 %_52)
  %_53 = load i32, i32* %i
  %_54 = add i32 %_53, 1
  store i32 %_54, i32* %i
  br label %while32
exit_while34:
  %_55 = load %_IntegerArray*, %_IntegerArray** %a
  %_56 = getelementptr %_IntegerArray, %_IntegerArray* %_55, i32 0, i32 0
  %_57 = load i32, i32* %_56
  %_58 = icmp slt i32 2, %_57
  %_59 = icmp sge i32 2, 0
  %_60 = and i1 %_59, %_58
  br i1 %_60, label %arr_look62, label %out_of_bounds61

out_of_bounds61:
  call void @throw_oob()
  br label %arr_look62

arr_look62:
  %_63 = getelementptr %_IntegerArray, %_IntegerArray* %_55, i32 0, i32 1
  %_64 = load i32* , i32** %_63
  %_65 = getelementptr i32, i32* %_64, i32 2
  store i32 10, i32* %_65
  %_66 = load %_IntegerArray*, %_IntegerArray** %a
  %_67 = getelementptr %_IntegerArray, %_IntegerArray* %_66, i32 0, i32 0
  %_68 = load i32, i32* %_67
  %_69 = icmp slt i32 3, %_68
  %_70 = icmp sge i32 3, 0
  %_71 = and i1 %_70, %_69
  br i1 %_71, label %arr_look73, label %out_of_bounds72

out_of_bounds72:
  call void @throw_oob()
  br label %arr_look73

arr_look73:
  %_74 = getelementptr %_IntegerArray, %_IntegerArray* %_66, i32 0, i32 1
  %_75 = load i32* , i32** %_74
  %_76 = getelementptr i32, i32* %_75, i32 3
  store i32 5, i32* %_76
  %_77 = load %_IntegerArray*, %_IntegerArray** %a
  %_78 = getelementptr %_IntegerArray, %_IntegerArray* %_77, i32 0, i32 0
  %_79 = load i32, i32* %_78
  %_80 = icmp slt i32 2, %_79
  %_81 = icmp sge i32 2, 0
  %_82 = and i1 %_81, %_80

  br i1 %_82, label %arr_look84, label %out_of_bounds83

out_of_bounds83:
  call void @throw_oob()
  br label %arr_look84

arr_look84:
  %_85 = getelementptr %_IntegerArray, %_IntegerArray* %_77, i32 0, i32 1
  %_86 = load i32* , i32** %_85
  %_87 = getelementptr i32, i32* %_86, i32 2
  %_88 = load i32, i32* %_87
  %_89 = icmp slt i32 %_88, 1
  br i1 %_89, label %andclause90, label %andclause91

andclause91:
  br label %andclause92

andclause90:
  %_93 = load %_IntegerArray*, %_IntegerArray** %a
  %_94 = getelementptr %_IntegerArray, %_IntegerArray* %_93, i32 0, i32 0
  %_95 = load i32, i32* %_94
  %_96 = icmp slt i32 3, %_95
  %_97 = icmp sge i32 3, 0
  %_98 = and i1 %_97, %_96

  br i1 %_98, label %arr_look100, label %out_of_bounds99

out_of_bounds99:
  call void @throw_oob()
  br label %arr_look100

arr_look100:
  %_101 = getelementptr %_IntegerArray, %_IntegerArray* %_93, i32 0, i32 1
  %_102 = load i32* , i32** %_101
  %_103 = getelementptr i32, i32* %_102, i32 3
  %_104 = load i32, i32* %_103
  %_105 = icmp slt i32 %_104, 1
  br label %andclause92

andclause92:
  %_106 = phi i1 [0, %andclause91], [%_105, %arr_look100]
  br i1 %_106, label %if107, label %else108

if107:
  call void (i32) @print_int(i32 1)
  br label %exit_if109
else108:
  %_110 = load %_IntegerArray*, %_IntegerArray** %a
  %_111 = getelementptr %_IntegerArray, %_IntegerArray* %_110, i32 0, i32 0
  %_112 = load i32, i32* %_111
  %_113 = icmp slt i32 2, %_112
  %_114 = icmp sge i32 2, 0
  %_115 = and i1 %_114, %_113

  br i1 %_115, label %arr_look117, label %out_of_bounds116

out_of_bounds116:
  call void @throw_oob()
  br label %arr_look117

arr_look117:
  %_118 = getelementptr %_IntegerArray, %_IntegerArray* %_110, i32 0, i32 1
  %_119 = load i32* , i32** %_118
  %_120 = getelementptr i32, i32* %_119, i32 2
  %_121 = load i32, i32* %_120
  call void (i32) @print_int(i32 %_121)
  br label %exit_if109
exit_if109:
  ret i32 0
}
