package my.chaster.extension.fitness

class ExtensionProperties {

	companion object {
		private const val PREFIX = "extension"
		private const val ENABLED = "enabled"

		const val STEPS_PER_PERIOD_ENABLED = "${PREFIX}.steps-per-period.${ENABLED}"
		const val STEPS_TO_UNLOCK_ENABLED = "${PREFIX}.steps-to-unlock.${ENABLED}"
	}
}
