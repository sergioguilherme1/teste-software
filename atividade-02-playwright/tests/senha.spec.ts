import { test, expect } from '@playwright/test';

const casos = [
  { senha: 'Abcde1', confirmacao: 'Abcde1', resultado: 'Senha fora do padrão', classe: 'abaixo do mínimo (6)' },
  { senha: 'Abcde123', confirmacao: 'Abcde123', resultado: 'Senha cadastrada', classe: 'limite mínimo (8)' },
  { senha: 'Abcccccccccccccccc12', confirmacao: 'Abcccccccccccccccc12', resultado: 'Senha cadastrada', classe: 'limite máximo (20)' },
  { senha: 'Abccccccccccccccccc12', confirmacao: 'Abccccccccccccccccc12', resultado: 'Senha fora do padrão', classe: 'acima do máximo (21)' },
  { senha: 'abcdefg1', confirmacao: 'abcdefg1', resultado: 'Senha fora do padrão', classe: 'sem maiúscula' },
  { senha: 'ABCDEFG1', confirmacao: 'ABCDEFG1', resultado: 'Senha fora do padrão', classe: 'sem minúscula' },
  { senha: 'Abcdefgh', confirmacao: 'Abcdefgh', resultado: 'Senha fora do padrão', classe: 'sem dígito' },
  { senha: 'Abcde 12', confirmacao: 'Abcde 12', resultado: 'Senha fora do padrão', classe: 'com espaço' },
  { senha: '', confirmacao: '', resultado: 'Senha fora do padrão', classe: 'vazia' },
  { senha: 'Abcde123', confirmacao: 'Abcde124', resultado: 'As senhas não coincidem', classe: 'confirmação divergente' },
  { senha: 'Senha123', confirmacao: 'Senha123', resultado: 'Senha cadastrada', classe: 'formato válido e confirmação idêntica' },
];

for (const caso of casos) {
  test(`senha ${caso.senha || '(vazia)'} — ${caso.classe}`, async ({ page }) => {
    await page.goto('/senha');
    await page.getByLabel('Nova senha').fill(caso.senha);
    await page.getByLabel('Confirmar senha').fill(caso.confirmacao);
    await page.getByRole('button', { name: 'Cadastrar senha' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();
    await expect(resultado).toHaveText(caso.resultado);
    await expect(resultado).toHaveAttribute('role', caso.resultado === 'Senha cadastrada' ? 'status' : 'alert');
  });
}
