package my.chaster

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor

@Configuration
class UnregisterScheduledProcessor : BeanFactoryPostProcessor {

	/**
	 * Deactivates all schedule annotations
	 */
	override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
		beanFactory as DefaultListableBeanFactory
		val beanNames = beanFactory.getBeanNamesForType(ScheduledAnnotationBeanPostProcessor::class.java)
		beanNames.forEach { beanFactory.removeBeanDefinition(it) }

		if (beanNames.isEmpty()) {
			throw IllegalStateException("Expected to remove some ScheduledAnnotationBeanPostProcessor to deactivate all scheduled methods")
		}
	}
}