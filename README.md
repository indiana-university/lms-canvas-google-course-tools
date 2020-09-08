# ess-lms-canvas-googleCourseTools

Requires a secret called `gctprops` with the following values:

```
gct.<env>.pickerApiKey=
gct.<env>.pickerClientId=
```
Where `<env>` represents an environment label, like `prd`, `stg`, `dev`, etc.

To Debug w/ Intellij:
```
kubectl apply -f portForward.yaml
```

```
helm upgrade googlecoursetools harbor-prd/k8s-boot -f helm-common.yaml -f helm-dev.yaml --install
```

```
helm upgrade googlecoursetools harbor-prd/k8s-boot -f helm-common.yaml -f helm-snd.yaml --install
```