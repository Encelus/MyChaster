package my.chaster.fitness

import my.chaster.chaster.WithChasterUserIdRepository
import my.chaster.jpa.AbstractEntityRepository


interface GoogleFitnessTokenRepository : AbstractEntityRepository<GoogleFitnessToken, GoogleFitnessTokenId>, WithChasterUserIdRepository<GoogleFitnessToken> {
	
}