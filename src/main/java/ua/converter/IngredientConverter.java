package ua.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ua.model.entity.Ingredient;
import ua.repository.IngredientRepository;

@Component
public class IngredientConverter implements Converter<String, Ingredient> {

	private final IngredientRepository repository;

	public IngredientConverter(IngredientRepository repository) {
		this.repository = repository;
	}

	@Override
	public Ingredient convert(String name) {
		return repository.findByName(name);
	}

}
