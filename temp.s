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
# your generated code here# ConstInt [1]: #0 <- 
# ConstInt [1]: %r9d <- 
mov $1, %r9d	# handleConst generateCode main
# --
# Add: #1 <- #0 #0
# Add: %r8d <- %r9d %r9d
mov %r9d, %r8d	# handleAddition generateCode main
add %r9d, %r8d	# handleAddition generateCode main
# --
# Add: #2 <- #1 #0
# Add: %r9d <- %r8d %r9d
add %r8d, %r9d	# handleAddition generateCode main
# --
# Add: #3 <- #2 #1
# Add: %r8d <- %r9d %r8d
add %r9d, %r8d	# handleAddition generateCode main
# --
# Add: #4 <- #3 #2
# Add: %r9d <- %r8d %r9d
add %r8d, %r9d	# handleAddition generateCode main
# --
# Add: #5 <- #4 #3
# Add: %r8d <- %r9d %r8d
add %r9d, %r8d	# handleAddition generateCode main
# --
# Add: #6 <- #5 #4
# Add: %r10d <- %r8d %r9d
mov %r9d, %r10d	# handleAddition generateCode main
add %r8d, %r10d	# handleAddition generateCode main
# --
# Add: #7 <- #6 #5
# Add: %r9d <- %r10d %r8d
mov %r8d, %r9d	# handleAddition generateCode main
add %r10d, %r9d	# handleAddition generateCode main
# --
# Add: #8 <- #7 #6
# Add: %r8d <- %r9d %r10d
mov %r10d, %r8d	# handleAddition generateCode main
add %r9d, %r8d	# handleAddition generateCode main
# --
# Add: #9 <- #8 #7
# Add: %r9d <- %r8d %r9d
add %r8d, %r9d	# handleAddition generateCode main
# --
# Add: #10 <- #9 #8
# Add: %r8d <- %r9d %r8d
add %r9d, %r8d	# handleAddition generateCode main
# --
# Add: #11 <- #10 #9
# Add: %r8d <- %r8d %r9d
add %r9d, %r8d	# handleAddition generateCode main
# --
# Return: #12! <- #11
# Return: % <- %r8d
mov %r8d, %eax	# handleReturn generateCode main
ret 	# handleReturn generateCode main
# --
