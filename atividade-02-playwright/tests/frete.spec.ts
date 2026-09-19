import { test, expect } from '@playwright/test';

const casos = [
  { cep: '80000000', valor: '50,00', resultado: 'Frete: R$ 15,00', classe: 'CEP iniciado por 8' },
  { cep: '01000000', valor: '50,00', resultado: 'Frete: R$ 25,00', classe: 'CEP não iniciado por 8' },
  { cep: '80000000', valor: '199,99', resultado: 'Frete: R$ 15,00', classe: 'valor limite inferior (199,99)' },
  { cep: '01000000', valor: '200,00', resultado: 'Frete grátis', classe: 'valor no limite (200,00)' },
  { cep: '80000000', valor: '200,01', resultado: 'Frete grátis', classe: 'valor acima do limite (200,01)' },
  { cep: '01000000', valor: '150.00', resultado: 'Frete: R$ 25,00', classe: 'valor com ponto decimal' },
  { cep: '01000000', valor: '300', resultado: 'Frete grátis', classe: 'valor inteiro sem decimais' },
  { cep: '8000000', valor: '50,00', resultado: 'Dados inválidos', classe: 'CEP com 7 dígitos' },
  { cep: '800000000', valor: '50,00', resultado: 'Dados inválidos', classe: 'CEP com 9 dígitos' },
  { cep: 'ABCDEFGH', valor: '50,00', resultado: 'Dados inválidos', classe: 'CEP com letras' },
  { cep: '', valor: '50,00', resultado: 'Dados inválidos', classe: 'CEP vazio' },
  { cep: '01000000', valor: '0', resultado: 'Dados inválidos', classe: 'valor zero' },
  { cep: '01000000', valor: '-50,00', resultado: 'Dados inválidos', classe: 'valor negativo' },
  { cep: '01000000', valor: '50,555', resultado: 'Dados inválidos', classe: 'valor com 3 casas decimais' },
  { cep: '01000000', valor: 'abc', resultado: 'Dados inválidos', classe: 'valor não numérico' },
  { cep: '01000000', valor: '', resultado: 'Dados inválidos', classe: 'valor vazio' },
];

for (const caso of casos) {
  test(`frete ${caso.cep || '(vazio)'}/${caso.valor || '(vazio)'} — ${caso.classe}`, async ({ page }) => {
    await page.goto('/frete');
    await page.getByLabel('CEP').fill(caso.cep);
    await page.getByLabel('Valor do pedido').fill(caso.valor);
    await page.getByRole('button', { name: 'Calcular frete' }).click();

    const resultado = page.locator('#resultado');
    await expect(resultado).toBeVisible();
    await expect(resultado).toHaveText(caso.resultado);
    await expect(resultado).toHaveAttribute('role', caso.resultado === 'Dados inválidos' ? 'alert' : 'status');
  });
}
