package my.chaster.messaging

interface MessagingConsumer<T> {

	fun handle(message: T)
}