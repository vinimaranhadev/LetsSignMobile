"""Sorteio do cardápio criativo (listas copiadas de prospect-local/scripts/cardapio-design.py)."""
import importlib.util, json, random, sys, os
spec = importlib.util.spec_from_file_location("c", sys.argv[1]); m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
seed = int(sys.argv[2]) if len(sys.argv) > 2 else random.SystemRandom().randrange(1, 10**6)
rng = random.Random(seed)
proib = m.PROIBIDAS_CRIATIVO
r = {"seed": seed,
     "cor": rng.choice(m.DIRECOES_COR),
     "composicao": rng.choice(m.COMPOSICOES),
     "ornamento": rng.choice(m.ORNAMENTOS),
     "display": rng.choice([f for f in m.FONTES_DISPLAY if f not in proib]),
     "corpo": rng.choice([f for f in m.FONTES_CORPO if f not in proib])}
print(json.dumps(r, ensure_ascii=False, indent=2))
