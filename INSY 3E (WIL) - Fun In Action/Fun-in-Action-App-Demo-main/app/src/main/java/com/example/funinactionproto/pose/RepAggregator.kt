package com.example.funinactionproto.pose


object RepAggregator {

    fun aggregate(bufferedFrames: List<Map<String, Float>>): Map<String, Float> {
        if (bufferedFrames.isEmpty()) return emptyMap()

        // Collect every feature name that appears in ANY frame
        val allFeatureNames = bufferedFrames.flatMap { it.keys }.toSet()

        val summary = mutableMapOf<String, Float>()
        for (name in allFeatureNames) {
            val values = bufferedFrames.mapNotNull { it[name] }
            if (values.isEmpty()) continue

            val min = values.min()
            val max = values.max()
            val mean = values.average().toFloat()

            summary["${name}_min"] = min
            summary["${name}_max"] = max
            summary["${name}_mean"] = mean
            summary["${name}_range"] = max - min
        }
        return summary
    }
}