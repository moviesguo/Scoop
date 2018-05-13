package com.binggege.scoop

/**
 * kotlin风格的条件检查 可用于空处理、Debug模式判断等
 * example:
 * <code>
 *     findViewById(R.id.view).whenNull {
 *         println("view is null")
 *     }.whenNotNull {
 *         it.setBackground(bg);
 *     }.also { it ->
 *         println("无论是不是null 都可以在最后跟一个操作符 就像这个also")
 *         println("但是需要注意 it 是否为空")
 *     }
 * <code>
 *
 * 注意 在两个操作符配合使用的时候 尽量不要混搭 以免影响阅读 推荐的搭配为
 * whenNotNull / whenNull
 * whenNotEmpty / whenEmpty
 *
 *
 * Created by congshengjie on 2018/3/28.
 */

/**
 * 判断对象是否为null 如果为null 则执行[action]
 * 在调用的过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenNotNull] 配合使用
 */
inline fun <T> T?.whenNull(action: () -> Unit): T? {
    if (this == null) {
        action()
    }
    return this
}

/**
 * 判断对象是否不为null 如果不为null 则执行[action]
 * 此非空对象作为形参传递到lambda中
 * 在调用过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenNull] 配合使用
 */
inline fun <T> T?.whenNotNull(action: (T) -> Unit): T? {
    if (this != null) {
        action(this)
    }
    return this
}

inline fun <T> T?.whichNotNull(action: T.() -> Unit): T? {
    this?.action()
    return this
}

/**
 * 判断字符串是否为空(null或无字符) 如果为空 则执行[action]
 * 在调用过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenNotEmpty] 配合使用
 * 因为函数是内联的 所以等价于
 * <code>
 *     if (str == null || str.isEmpty()) {
 *         action();
 *     }
 * </code>
 */
inline fun <T :CharSequence> T?.whenEmpty(action: () -> Unit): T? {
    if (this == null || isEmpty()) {
        action()
    }
    return this
}

/**
 * 判断Collection是否为空(null或长度为0) 如果为空 则执行[action]
 * 在调用过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenNotEmpty] 配合使用
 */
inline fun <T, U : Collection<T>> U?.whenEmpty(action: () -> Unit): U? {
    if (this == null || isEmpty()) {
        action()
    }
    return this
}

/**
 * 判断字符串是否为不空(有字符) 如果为不为空 则执行[action]
 * 此非空字符作为形参传递到lambda中
 * 在调用过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenEmpty] 配合使用
 */
inline fun <T :CharSequence> T?.whenNotEmpty(action: (T) -> Unit): T? {
    if (this != null && isNotEmpty()) {
        action(this)
    }
    return this
}

/**
 * 判断Collection是否为空(有元素) 如果为不为空 则执行[action]
 * 此非空字符作为形参传递到lambda中
 * 在调用过程中不需要通过'?.'访问 可与 [com.mfw.roadbook.utils.whenEmpty] 配合使用
 */
inline fun <T, U : Collection<T>> U?.whenNotEmpty(action: (U) -> Unit): U? {
    if (this != null && isNotEmpty()) {
        action(this)
    }
    return this
}