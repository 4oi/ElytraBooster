/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 misterT2525
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jp.llv.eb;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Utility class for item cool-down
 *
 * @author misterT2525
 */
public final class ItemCooldownUtil {
    private ItemCooldownUtil() {
    }

    public static void setCooldown(Player player, Material item, int ticks) throws NullPointerException, IllegalArgumentException {
        if (player == null) throw new NullPointerException("player");
        if (item == null) throw new NullPointerException("item");

        Object internalItem = convertItem(item);
        if (internalItem == null) throw new IllegalArgumentException("item");

        invokeMethod(getSetCooldownMethod(), getItemCooldown(player), internalItem, ticks);
    }

    public static boolean inCooldown(Player player, Material item) throws NullPointerException, IllegalArgumentException {
        if (player == null) throw new NullPointerException("player");
        if (item == null) throw new NullPointerException("item");

        Object internalItem = convertItem(item);
        if (internalItem == null) throw new IllegalArgumentException("item");

        return (boolean) invokeMethod(getInCooldownMethod(), getItemCooldown(player), internalItem);
    }

    /*========== Internal ==========*/

    private static Method GET_HANDLE;
    private static Method GET_ITEM_COOLDOWN;
    private static Method CONVERT_ITEM;

    private static Object getItemCooldown(Player player) {
        if (GET_HANDLE == null)
            GET_HANDLE = getMethodByName(getCraftBukkitClass("entity.CraftPlayer"), "getHandle");
        Object handle = invokeMethod(GET_HANDLE, player);

        if (GET_ITEM_COOLDOWN == null)
            GET_ITEM_COOLDOWN = getMethodByTypes(getMinecraftClass("EntityHuman"), "getItemCooldown", getItemCooldownClass());
        return invokeMethod(GET_ITEM_COOLDOWN, handle);
    }

    private static Object convertItem(Material item) {
        if (CONVERT_ITEM == null)
            CONVERT_ITEM = getMethodByName(getCraftBukkitClass("util.CraftMagicNumbers"), "getItem", Material.class);
        return invokeMethod(CONVERT_ITEM, null, item);
    }

    private static Method SET_COOLDOWN;
    private static Method IN_COOLDOWN;
    private static Class<?> ITEM_COOLDOWN_CLASS;
    private static Class<?> ITEM_CLASS;

    private static Method getSetCooldownMethod() {
        if (SET_COOLDOWN == null)
            SET_COOLDOWN = getMethodByTypes(getItemCooldownClass(), "setCooldown", void.class, getItemClass(), int.class);
        return SET_COOLDOWN;
    }

    private static Method getInCooldownMethod() {
        if (IN_COOLDOWN == null)
            IN_COOLDOWN = getMethodByTypes(getItemCooldownClass(), "inCooldown", boolean.class, getItemClass());
        return IN_COOLDOWN;
    }

    private static Class<?> getItemCooldownClass() {
        if (ITEM_COOLDOWN_CLASS == null)
            ITEM_COOLDOWN_CLASS = getMinecraftClass("ItemCooldown");
        return ITEM_COOLDOWN_CLASS;
    }

    private static Class<?> getItemClass() {
        if (ITEM_CLASS == null)
            ITEM_CLASS = getMinecraftClass("Item");
        return ITEM_CLASS;
    }

    /*========== Reflection ==========*/

    private static final String OBC_PACKAGE = Bukkit.getServer().getClass().getPackage().getName() + ".";
    private static final String NMS_PACKAGE = OBC_PACKAGE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    private static Class<?> getCraftBukkitClass(String name) {
        return getClass(OBC_PACKAGE + name);
    }

    private static Class<?> getMinecraftClass(String name) {
        return getClass(NMS_PACKAGE + name);
    }

    private static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(name + " not found", e);
        }
    }

    private static Method getMethodByName(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(name + " not found in " + clazz, e);
        }
    }

    private static Method getMethodByTypes(Class<?> clazz, String name, Class<?> returnType, Class<?>... parameterTypes) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        throw new RuntimeException(name + " not found in " + clazz);
    }

    private static Object invokeMethod(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}