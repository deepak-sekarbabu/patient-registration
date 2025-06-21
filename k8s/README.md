# Patient Registration - Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the Patient Registration application.

## Prerequisites

- Docker installed and running
- Kubernetes cluster (Minikube, Docker Desktop, or cloud-based)
- `kubectl` configured to communicate with your cluster

## Building the Docker Image

1. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```

2. Build the Docker image:
   ```bash
   docker build -t patient-registration:latest .
   ```

3. (Optional) Tag and push to a container registry:
   ```bash
   docker tag patient-registration:latest your-registry/patient-registration:latest
   docker push your-registry/patient-registration:latest
   ```
   Update the image name in `01-deployment.yaml` if using a different registry.

## Deploying to Kubernetes

1. Apply the Kubernetes manifests in order:
   ```bash
   kubectl apply -f 00-namespace.yaml
   kubectl apply -f 03-configmap.yaml
   kubectl apply -f 01-deployment.yaml
   kubectl apply -f 02-service.yaml
   # Optional: Only if you have an Ingress controller
   # kubectl apply -f 04-ingress.yaml
   ```

2. Verify the deployment:
   ```bash
   kubectl get all -n patient-registration
   ```

3. Port-forward to access the application:
   ```bash
   kubectl port-forward -n patient-registration svc/patient-registration 8080:80
   ```
   Access the application at: http://localhost:8080

## Configuration

- Update `03-configmap.yaml` for environment-specific configurations.
- Adjust resource requests/limits in `01-deployment.yaml` as needed.
- Modify `04-ingress.yaml` with your domain and TLS settings.

## Scaling

To scale the application:

```bash
kubectl scale -n patient-registration deployment/patient-registration --replicas=3
```

## Cleanup

To remove all resources:

```bash
kubectl delete -f .
# Or delete the entire namespace
kubectl delete namespace patient-registration
```
