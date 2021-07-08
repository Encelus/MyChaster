package my.chaster.chaster

import javax.persistence.AttributeConverter
import javax.persistence.Converter

data class ChasterUserId(val id: String)

@Converter(autoApply = true)
class ChasterIdConverter : AttributeConverter<ChasterUserId?, String?> {

	override fun convertToDatabaseColumn(attribute: ChasterUserId?): String? {
		return attribute?.id
	}

	override fun convertToEntityAttribute(dbData: String?): ChasterUserId? {
		return dbData?.let { ChasterUserId(it) }
	}

}