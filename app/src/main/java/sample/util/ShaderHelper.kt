package sample.util

import android.opengl.GLES20.*
import android.util.Log

val TAG = "ShaderHelper"

fun validateProgram(programObjectId: Int): Boolean {
    glValidateProgram(programObjectId)

    val validateStatus = IntArray(1)
    glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
    Log.v(
        TAG,
        "Results of validating program: ${validateStatus[0]} \n Log: ${glGetProgramInfoLog(
            programObjectId
        )}"
    )

    return validateStatus[0] != 0
}

fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
    val programObjectId = glCreateProgram()

    if (programObjectId == 0) {
        Log.w(TAG, "Could not create new program")

        return 0
    }

    glAttachShader(programObjectId, vertexShaderId)
    glAttachShader(programObjectId, fragmentShaderId)

    glLinkProgram(programObjectId)

    // Check whether link failed or succeeded. Store link result in compileStatus
    val linkStatus = IntArray(1)
    glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
    Log.v(
        TAG,
        "Results of linking program: \n ${glGetProgramInfoLog(programObjectId)}"
    )
    if (linkStatus[0] == 0) {
        // if compile failed, delete shader object
        glDeleteProgram(programObjectId)

        Log.w(TAG, "Linking the program failed")

        return 0
    }

    return programObjectId
}

fun compileVertexShader(shaderCode: String): Int {
    return compileShader(GL_VERTEX_SHADER, shaderCode)
}

fun compileFragmentShader(shaderCode: String): Int {
    return compileShader(GL_FRAGMENT_SHADER, shaderCode)
}

fun compileShader(type: Int, shaderCode: String): Int {
    val shaderObjectId = glCreateShader(type) // returns reference to OpenGL object

    if (shaderObjectId == 0) {
        Log.w(TAG, "Could not create new shader")

        return 0
    }

    // read in the source code defined in the String shaderCode and associate it
    // with the shader object referred to by shaderObjectId
    glShaderSource(shaderObjectId, shaderCode)

    glCompileShader(shaderObjectId)

    // Check whether compile failed or succeeded. Store compile result in compileStatus
    val compileStatus = IntArray(1)
    glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)
    Log.v(
        TAG,
        "Results of compiling source: \n ${shaderCode}\n ${glGetShaderInfoLog(shaderObjectId)}"
    )

    if (compileStatus[0] == 0) {
        // if compile failed, delete shader object
        glDeleteShader(shaderObjectId)

        Log.w(TAG, "Compilation of shader failed")

        return 0
    }

    return shaderObjectId
}
