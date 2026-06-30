.PHONY: build build-jar run run-d recalc

build:
	docker compose build

build-jar:
	cd server/ && ./gradlew shadowJar

run:
	docker compose up

run-d:
	docker compose up -d

recalc:
	docker compose run --rm --remove-orphans server --recalc