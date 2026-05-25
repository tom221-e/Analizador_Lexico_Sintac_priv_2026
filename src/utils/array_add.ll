; ModuleID = 'array_add'
source_filename = "array_add.ll"

target triple = "x86_64-pc-windows-msvc"

target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-f80:128-n8:16:32:64-S128"


@fmt = private unnamed_addr constant [4 x i8] c"%f\0A\00"

declare i32 @printf(ptr, ...)


; result[i] = a[i] + b[i]
define void @sum_arrays(ptr %a, ptr %b, ptr %result, i32 %n) {

	entry:

		; i = 0
		%i = alloca i32
		store i32 0, ptr %i

		br label %loop_cond


	loop_cond:

		; cargar i
		%i_val = load i32, ptr %i

		; i < n
		%cond = icmp slt i32 %i_val, %n

		br i1 %cond, label %loop_body, label %exit


	loop_body:

		; a[i]

		%a_ptr = getelementptr double, ptr %a, i32 %i_val

		%a_elem = load double, ptr %a_ptr


		; b[i]

		%b_ptr = getelementptr double, ptr %b, i32 %i_val

		%b_elem = load double, ptr %b_ptr


		; a[i] + b[i]
		%sum = fadd double %a_elem, %b_elem


		; result[i]

		%r_ptr = getelementptr double, ptr %result, i32 %i_val

		store double %sum, ptr %r_ptr


		; i++
		%next = add i32 %i_val, 1
		store i32 %next, ptr %i

		br label %loop_cond


	exit:
		ret void
}



define i32 @main() {

	entry:

		; array A

		%array_a = alloca [5 x double]

		store [5 x double] [double 1.0, double 2.0, double 3.0, double 4.0, double 5.0], ptr %array_a

		; array B

		%array_b = alloca [5 x double]

		store [5 x double] [double 10.0, double 20.0, double 30.0, double 40.0, double 50.0], ptr %array_b

		; array resultado

		%result_array = alloca [5 x double]

		; &array_a[0]
		%a_data = getelementptr [5 x double], ptr %array_a, i32 0, i32 0

		; &array_b[0]
		%b_data = getelementptr [5 x double], ptr %array_b, i32 0, i32 0

		; &result_array[0]
		%result_data = getelementptr [5 x double], ptr %result_array, i32 0, i32 0


		; result = a + b
		call void @sum_arrays(ptr %a_data, ptr %b_data, ptr %result_data, i32 5)


		; -------------------------
		; imprimir resultado
		; -------------------------

		%i = alloca i32
		store i32 0, ptr %i

		br label %print_cond


	print_cond:

		%i_val = load i32, ptr %i

		%cond = icmp slt i32 %i_val, 5

		br i1 %cond, label %print_body, label %exit


	print_body:

		; result_array[i]
		%elem_ptr = getelementptr [5 x double], ptr %result_array,i32 0, i32 %i_val

		%elem = load double, ptr %elem_ptr

		; "%f\n"
		%fmt_ptr = getelementptr [4 x i8], ptr @fmt, i32 0, i32 0


		; printf("%f\n", elem)
		call i32 (ptr, ...) @printf(ptr %fmt_ptr, double %elem)

		; i++
		%next = add i32 %i_val, 1
		store i32 %next, ptr %i

		br label %print_cond


	exit:
		ret i32 0
}