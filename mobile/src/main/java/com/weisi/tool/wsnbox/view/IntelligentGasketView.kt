package com.weisi.tool.wsnbox.view

import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.cjq.lib.weisi.iot.interpreter.FloatInterpreter
import com.weisi.tool.wsnbox.R
import kotlinx.android.synthetic.main.group_intelligent_gasket.view.*

class IntelligentGasketView : ConstraintLayout {

    companion object {
        private val VALUE_INTERPRETER = FloatInterpreter(3, "KN")
        private const val MIN_ANIMATION_TIME_INTERVAL = 0.005
        private const val OVERFLOW_RATIO = 0.5
        private const val MIN_LOW_LIMIT_BIAS = 0.7f
        private const val MAX_LOW_LIMIT_BIAS = 0.83f
        private const val MIN_HIGH_LIMIT_BIAS = 0.17f
        private const val MAX_HIGH_LIMIT_BIAS = 0.3f
    }

    private lateinit var transition: Transition
    private var topValue = 100.0
    private var bottomValue = 0.0
    private var lowLimit = 25.0
    private var highLimit = 75.0
    var realTimeValue: Double? = null
        set(value) {
            val prevValue = field
            if (value == prevValue) {
                return
            }
            val tvRealTimeValue = tv_real_time_value ?: return
            if (value === null) {
                tvRealTimeValue.visibility = View.INVISIBLE
                setRealTimeLabelPositionByAnimation(1.0f)
            } else {
                if (prevValue === null) {
                    tvRealTimeValue.visibility = View.VISIBLE
                    setRealTimeLabelPositionByAnimation(value)
                } else {
                    if (Math.abs(prevValue - value) < MIN_ANIMATION_TIME_INTERVAL) {
                        setRealTimeLabelPositionDirectly(value)
                    } else {
                        setRealTimeLabelPositionByAnimation(value)
                    }
                }
                tvRealTimeValue.text = VALUE_INTERPRETER.interpret(value)
            }
            field = value
        }

    private fun setRealTimeLabelPositionDirectly(value: Double)
            = setRealTimeLabelPositionDirectly(calculateTargetBias(value))

    private fun setRealTimeLabelPositionDirectly(bias: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.setVerticalBias(R.id.tv_real_time_value, bias)
        constraintSet.applyTo(this)
    }

    private fun setRealTimeLabelPositionByAnimation(value: Double)
            = setRealTimeLabelPositionByAnimation(calculateTargetBias(value))

    private fun setRealTimeLabelPositionByAnimation(bias: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(this, transition)
        }
        setRealTimeLabelPositionDirectly(bias)
    }

    private fun calculateTargetBias(value: Double): Float {
        val theoryBias = (1 - (value - bottomValue) / (topValue - bottomValue)).toFloat()
        var actualBias = 0.0f
        if (value < bottomValue) {
            actualBias = 1.0f
        } else if (value < topValue) {
            if (theoryBias in MIN_LOW_LIMIT_BIAS..MAX_LOW_LIMIT_BIAS) {
                if (value > lowLimit) {
                    actualBias = MIN_LOW_LIMIT_BIAS
                } else {
                    actualBias = MAX_LOW_LIMIT_BIAS
                }
            } else if (theoryBias in MIN_HIGH_LIMIT_BIAS..MAX_HIGH_LIMIT_BIAS) {
                if (value < highLimit) {
                    actualBias = MAX_HIGH_LIMIT_BIAS
                } else {
                    actualBias = MIN_HIGH_LIMIT_BIAS
                }
            } else {
                actualBias = theoryBias
            }
        }
        return actualBias
    }

//    fun calculateTargetBias(value: Double): Float {
//        val theoryBias = ((value - bottomValue) / (topValue - bottomValue)).toFloat()
//        var actualBias = 0.0f
//        if (value < bottomValue) {
//            actualBias = 1.0f
//        } else if (value < topValue) {
//            if (theoryBias in MIN_LOW_LIMIT_BIAS..MAX_LOW_LIMIT_BIAS) {
//                if (value > lowLimit) {
//                    actualBias = MIN_LOW_LIMIT_BIAS
//                } else {
//                    actualBias = MAX_LOW_LIMIT_BIAS
//                }
//            } else if (theoryBias in MIN_HIGH_LIMIT_BIAS..MAX_HIGH_LIMIT_BIAS) {
//                if (value < highLimit) {
//                    actualBias = MAX_HIGH_LIMIT_BIAS
//                } else {
//                    actualBias = MIN_HIGH_LIMIT_BIAS
//                }
//            } else {
//                actualBias = theoryBias
//            }
//        }
//        return actualBias
//    }

    constructor(context: Context)
            : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.group_intelligent_gasket, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //transition = TransitionInflater.from(context).inflateTransition(R.transition.gasket_value_change)
            transition = AutoTransition()
            transition.duration = 1000
        }
    }

    fun setLabel(name: String) {
        tv_gasket_name.text = name
    }

    fun setCoordinate(coordinate: String) {
        tv_gasket_coordinate.text = coordinate
    }

    fun setLimit(low: Double, high: Double) {
        if (low >= high) {
            return
        }
        lowLimit = low
        highLimit = high
        val overflowValue = (high - low) * OVERFLOW_RATIO
        bottomValue = low - overflowValue
        topValue = high + overflowValue
        val tensionInterpreter = FloatInterpreter(0, "KN")
        tv_low_limit.text = tensionInterpreter.interpret(lowLimit)
        tv_high_limit.text = tensionInterpreter.interpret(highLimit)
        realTimeValue = realTimeValue
    }
}