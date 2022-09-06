package com.app.personas_social.videoEditing

import java.lang.StringBuilder
import java.util.ArrayList

class CmdList : ArrayList<String?>() {
    fun append(s: String?): CmdList {
        this.add(s)
        return this
    }

    fun append(i: Int): CmdList {
        this.add(i.toString() + "")
        return this
    }

    fun append(f: Float): CmdList {
        this.add(f.toString() + "")
        return this
    }

    fun append(sb: StringBuilder): CmdList {
        this.add(sb.toString())
        return this
    }

    fun append(ss: Array<String>): CmdList {
        for (s in ss) {
            if (s.replace(" ", "") != "") {
                this.add(s)
            }
        }
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (s in this) {
            sb.append(" ").append(s)
        }
        return sb.toString()
    }
}