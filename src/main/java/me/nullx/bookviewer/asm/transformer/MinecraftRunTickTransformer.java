package me.nullx.bookviewer.asm.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;

public class MinecraftRunTickTransformer implements IClassTransformer {

    /**
     *
     * Minecraft$runTick()
     * Line 1852
     *
     * L71
     *     LINENUMBER 1852 L71
     *     ALOAD 0
     *     GETFIELD net/minecraft/client/Minecraft.inGameHasFocus : Z
     *     IFNE L56
     *     INVOKESTATIC org/lwjgl/input/Mouse.getEventButtonState ()Z
     *     IFEQ L56
     */

    private static final String[] classesBeingTransformed = {
            "net.minecraft.client.Minecraft"
    };

    private static final String[] classesBeingTransformedObf = {
            "ave"
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        //System.out.println("TRANSFORM CALLED FOR " + name + " WITH TN " + transformedName);
        //boolean isObfuscated = !name.equalsIgnoreCase(transformedName);
        boolean isObfuscated = true;
        int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
        if (index == -1) {
            index = Arrays.asList(classesBeingTransformedObf).indexOf(transformedName);
        }
        return index != -1 ? transform(index, basicClass, isObfuscated) : basicClass;
    }

    private byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated) {
        System.out.println("CALLED TRANSFORMER FOR CLASS");
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            switch (index) {
                case 0:
                    transformMinecraftClass(classNode, isObfuscated);
                    break;
            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new byte[0];
    }

    private void transformMinecraftClass(ClassNode minecraftClass, boolean isObfuscated) {
        final String RUN_TICK = isObfuscated ? "s" : "runTick";
        final String RUN_TICK_DESC = isObfuscated ? "()V" : "()V";
        final String INGAME_HASFOCUS_NAME = isObfuscated ? "w" : "inGameHasFocus";
        final String EVENT_BUTTON_STATE = "getEventButtonState";

        for (MethodNode methodNode : minecraftClass.methods) {
            if (methodNode.name.equals(RUN_TICK) && methodNode.desc.equals(RUN_TICK_DESC)) {
                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                    if (instruction.getOpcode() == ALOAD && ((VarInsnNode) instruction).var == 0
                            && instruction.getNext().getOpcode() == GETFIELD && ((FieldInsnNode) instruction.getNext()).name.equals(INGAME_HASFOCUS_NAME)
                            && instruction.getNext().getNext().getNext().getOpcode() == INVOKESTATIC && ((MethodInsnNode) instruction.getNext().getNext().getNext()).name.equals(EVENT_BUTTON_STATE)) {
                        targetNode = instruction;
                    }
                }
                if (targetNode != null) {
                    InsnList insnList = new InsnList();
                    insnList.add(new MethodInsnNode(INVOKESTATIC, "me/nullx/bookviewer/BookViewer", "handleMouseInput", "()V"));
                    methodNode.instructions.insertBefore(targetNode, insnList);
                }
            }
        }
    }
}
