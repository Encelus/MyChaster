package my.chaster.jpa

import java.time.Duration
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class DurationConverter : AttributeConverter<Duration?, String?> {

	override fun convertToDatabaseColumn(attribute: Duration?): String? {
		return attribute?.toString()
	}

	override fun convertToEntityAttribute(dbData: String?): Duration? {
		return dbData?.let { Duration.parse(it) }
	}
}