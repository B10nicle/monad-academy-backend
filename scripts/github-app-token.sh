#!/usr/bin/env bash
set -euo pipefail

app_id="${GITHUB_APP_ID:-${MONAD_GITHUB_APP_ID:-}}"
installation_id="${GITHUB_APP_INSTALLATION_ID:-${MONAD_GITHUB_APP_INSTALLATION_ID:-}}"

if [[ -z "$app_id" ]]; then
  echo "Set GITHUB_APP_ID or MONAD_GITHUB_APP_ID to the GitHub App ID" >&2
  exit 1
fi

if [[ -z "$installation_id" ]]; then
  echo "Set GITHUB_APP_INSTALLATION_ID or MONAD_GITHUB_APP_INSTALLATION_ID to the GitHub App installation ID" >&2
  exit 1
fi

private_key_path="${GITHUB_APP_PRIVATE_KEY_PATH:-${MONAD_GITHUB_APP_PRIVATE_KEY_PATH:-$PWD/.secrets/monad-academy-agent.private-key.pem}}"
private_key="${GITHUB_APP_PRIVATE_KEY:-${MONAD_GITHUB_APP_PRIVATE_KEY:-}}"

node --input-type=module - "$app_id" "$installation_id" "$private_key_path" "$private_key" <<'NODE'
import { createSign } from 'node:crypto';
import { readFileSync } from 'node:fs';

const [, , appId, installationId, privateKeyPath, privateKeyFromEnv] = process.argv;

function base64Url(value) {
  return Buffer.from(value)
    .toString('base64')
    .replaceAll('=', '')
    .replaceAll('+', '-')
    .replaceAll('/', '_');
}

const now = Math.floor(Date.now() / 1000);
const header = { alg: 'RS256', typ: 'JWT' };
const payload = {
  iat: now - 60,
  exp: now + 540,
  iss: appId,
};

const unsignedToken = `${base64Url(JSON.stringify(header))}.${base64Url(JSON.stringify(payload))}`;
const privateKey = privateKeyFromEnv || readFileSync(privateKeyPath, 'utf8');
const signature = createSign('RSA-SHA256')
  .update(unsignedToken)
  .sign(privateKey, 'base64')
  .replaceAll('=', '')
  .replaceAll('+', '-')
  .replaceAll('/', '_');

const jwt = `${unsignedToken}.${signature}`;
const response = await fetch(
  `https://api.github.com/app/installations/${installationId}/access_tokens`,
  {
    method: 'POST',
    headers: {
      Accept: 'application/vnd.github+json',
      Authorization: `Bearer ${jwt}`,
      'X-GitHub-Api-Version': '2022-11-28',
    },
  },
);

if (!response.ok) {
  const body = await response.text();
  console.error(`GitHub App token request failed: ${response.status} ${response.statusText}`);
  console.error(body);
  process.exit(1);
}

const { token } = await response.json();
process.stdout.write(token);
NODE
