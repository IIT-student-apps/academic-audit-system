import nltk
from nltk.tokenize import word_tokenize, sent_tokenize
from nltk import pos_tag, ne_chunk
from collections import Counter


class TextAnalyzer:
	def __init__(self):
		# Загрузка необходимых ресурсов NLTK
		nltk.download('punkt')
		nltk.download('averaged_perceptron_tagger')
		nltk.download('maxent_ne_chunker')
		nltk.download('words')

	def full_analysis(self, text):
		"""Выполняет полный анализ текста"""
		return {
			"morphology": self.analyze_morphology(text),
			"syntax": self.analyze_syntax(text),
			"semantics": self.analyze_semantics(text),
			"statistics": self.get_text_statistics(text)
		}

	def analyze_morphology(self, text):
		"""Анализ морфологии (части речи)"""
		tokens = word_tokenize(text)
		pos_tags = pos_tag(tokens)
		return {
			"word_count": len(tokens),
			"pos_tags": dict(Counter(tag for word, tag in pos_tags))
		}

	def analyze_syntax(self, text):
		"""Анализ синтаксиса (структура предложений)"""
		sentences = sent_tokenize(text)
		return {
			"sentence_count": len(sentences),
			"avg_sentence_length": sum(len(sent.split()) for sent in sentences) / len(sentences) if sentences else 0
		}

	def analyze_semantics(self, text):
		"""Анализ семантики (именованные сущности)"""
		tokens = word_tokenize(text)
		pos_tags = pos_tag(tokens)
		named_entities = ne_chunk(pos_tags)

		entities = []
		for chunk in named_entities:
			if hasattr(chunk, 'label'):
				entities.append({
					"label": chunk.label(),
					"text": " ".join(c[0] for c in chunk)
				})

		return {
			"named_entities": entities,
			"unique_entities": len(set(e["text"] for e in entities))
		}

	def get_text_statistics(self, text):
		"""Собирает статистику по тексту"""
		words = word_tokenize(text)
		word_count = len(words)
		unique_words = len(set(words.lower() for words in words))

		return {
			"word_count": word_count,
			"unique_word_count": unique_words,
			"lexical_diversity": unique_words / word_count if word_count > 0 else 0
		}
