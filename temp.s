.global main
.global _main
.text
main:
call _main
# move the return value into the first argument for the syscall
movq %rax, %rdi
# move the exit syscall number into rax
movq $0x3C, %rax
syscall
_main:
# your generated code here# ConstInt [0]: #0 <- 
# ConstInt [0]: %r8d <- 
# None
mov $0, %r8d	# handleConst generateCode main
# --
# Return: #1! <- #0
# Return: % <- %r8d
# None
mov %r8d, %eax	# handleReturn generateCode main
ret 	# handleReturn generateCode main
# --
