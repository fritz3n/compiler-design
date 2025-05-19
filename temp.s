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
# ConstInt [0]: %r9d <- 
mov $0, %r9d	# handleConst generateCode main
# --
# ConstInt [1]: #1 <- 
# ConstInt [1]: %r8d <- 
mov $1, %r8d	# handleConst generateCode main
# --
# Sub: #2 <- #0 #1
# Sub: %r8d <- %r9d %r8d
mov %r9d, %eax	# handleSubtraction generateCode main
mov %r8d, %edx	# handleSubtraction generateCode main
sub %edx, %eax	# handleSubtraction generateCode main
mov %eax, %r8d	# handleSubtraction generateCode main
# --
# Div: #3 <- #2 #2
# Div: %r8d <- %r8d %r8d
mov %r8d, %eax	# handleDividingBinary generateCode main
cltd 	# handleDividingBinary generateCode main
idiv %r8d	# handleDividingBinary generateCode main
mov %eax, %r8d	# handleDividingBinary generateCode main
# --
# Return: #4! <- #3
# Return: % <- %r8d
mov %r8d, %eax	# handleReturn generateCode main
ret 	# handleReturn generateCode main
# --
