# Deploy securing-web to Kubernetes (Windows + Docker Desktop)

This guide helps you run the Spring Boot app in Kubernetes using the manifests in the `k8s/` folder. It assumes Windows with Docker Desktop.

## Prerequisites
- Docker Desktop with Kubernetes enabled
- `kubectl` available in your PATH (`kubectl version`)
- Optional: Ingress controller (e.g. `ingress-nginx`) if you want to use the Ingress

## Quick start (No Auth mode)
We added a profile `noauth` that disables Auth0 and permits all requests. In this mode you do NOT need any Auth0 values.

1) Build the Docker image

```
cd /d "C:\Users\lmqua\OneDrive\Desktop\Nam 3\gs-securing-web-main"
docker build -t securing-web:latest .
```

2) Deploy to Kubernetes (uses profile `noauth` automatically via Deployment env)

```
kubectl apply -k k8s
kubectl -n securing-web get pods
```

3) Open the app
- NodePort: http://localhost:30085/hello and http://localhost:30085/
- Ingress (if installed): http://app.localtest.me/

Notes
- In `noauth` mode, the ConfigMap and Secret are not needed. They can remain in the repo; the Deployment doesn't read them.
- The app uses H2 in-memory DB; data resets on restart.

## Auth0 mode (optional)
If you later want to enable Auth0 login/OIDC, deploy without the `noauth` profile and set the following:
- Update `k8s/configmap.yaml`:
  - `AUTH0_DOMAIN`: your tenant domain like `your-tenant.us.auth0.com`
  - `AUTH0_AUDIENCE` (optional): if your API uses an audience
- Update `k8s/secret.yaml`:
  - `AUTH0_CLIENT_ID`: your application client ID
  - `AUTH0_CLIENT_SECRET`: your application client secret
- Change Deployment to remove `SPRING_PROFILES_ACTIVE=noauth` and add `envFrom` for ConfigMap/Secret.
- Update Auth0 Application Allowed Callback URLs, e.g.:
  - `http://localhost:30085/login/oauth2/code/auth0`
  - `http://app.localtest.me/login/oauth2/code/auth0`

## Troubleshooting
- Pods and logs:
  - `kubectl -n securing-web get pods`
  - `kubectl -n securing-web logs deploy/securing-web`
- Describe resources:
  - `kubectl -n securing-web describe deploy/securing-web`
  - `kubectl -n securing-web describe svc/securing-web`
- Probes target `/hello`. If you change routes, update the probes.

## Remove
```
kubectl delete -k k8s
```
