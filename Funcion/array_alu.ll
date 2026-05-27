; ModuleID = 'array_alu'
source_filename = "array_alu.ll"

target triple = "x86_64-pc-windows-msvc"
target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-f80:128-n8:16:32:64-S128"

@fmt = private unnamed_addr constant [4 x i8] c"%f\0A\00"
declare i32 @printf(ptr, ...)

; =========================================================================
; FUNCIÓN GENÉRICA DE OPERACIÓN DE ARREGLOS
; @op = 1 (Suma), 2 (Resta), 3 (División), 4 (Multiplicación)
; inputs: ptr %a, ptr %b, ptr %result -> Apuntan a listas de tipo 'double'
; =========================================================================
define void @operar_arreglos(ptr %a, ptr %b, ptr %result, i32 %n, i32 %op) {

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
		; 🌟 CORRECCIÓN CRÍTICA: Convertimos el índice i32 a i64 para direccionar memoria de 64-bits
		%i_val_64 = sext i32 %i_val to i64

		; Obtener puntero y cargar a[i] (Usa double y el índice i64)
		%a_ptr = getelementptr double, ptr %a, i64 %i_val_64
		%a_elem = load double, ptr %a_ptr

		; Obtener puntero y cargar b[i] (Usa double y el índice i64)
		%b_ptr = getelementptr double, ptr %b, i64 %i_val_64
		%b_elem = load double, ptr %b_ptr

		; -----------------------------------------------------------------
		; SWITCH: Evalúa el parámetro %op para decidir a qué bloque saltar
		; -----------------------------------------------------------------
		switch i32 %op, label %op_suma [
			i32 1, label %op_suma
			i32 2, label %op_resta
			i32 3, label %op_div
			i32 4, label %op_multi
		]

	op_suma:
		%res_suma = fadd double %a_elem, %b_elem
		br label %guardar_resultado

	op_resta:
		%res_resta = fsub double %a_elem, %b_elem
		br label %guardar_resultado

	op_div:
		%res_div = fdiv double %a_elem, %b_elem
		br label %guardar_resultado

	op_multi:
		%res_multi = fmul double %a_elem, %b_elem
		br label %guardar_resultado

	guardar_resultado:
		; PHI node: Recoge el valor calculado dependiendo de qué bloque venimos (estricto double)
		%val_final = phi double [ %res_suma, %op_suma ], 
		                        [ %res_resta, %op_resta ], 
		                        [ %res_div, %op_div ], 
		                        [ %res_multi, %op_multi ]

		; Guardar en result[i] (Usa double y el índice i64)
		%r_ptr = getelementptr double, ptr %result, i64 %i_val_64
		store double %val_final, ptr %r_ptr

		; i++
		%next = add i32 %i_val, 1
		store i32 %next, ptr %i
		br label %loop_cond

	exit:
		ret void
}