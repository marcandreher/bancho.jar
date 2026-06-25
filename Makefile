.PHONY: build run run-d recalc

build:
	docker compose build

run:
	docker compose up

run-d:
	docker compose up -d

recalc:
	docker compose run --rm --remove-orphans server --recalc