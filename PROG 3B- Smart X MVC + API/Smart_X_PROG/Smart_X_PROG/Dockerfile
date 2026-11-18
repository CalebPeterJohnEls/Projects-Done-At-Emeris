FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
WORKDIR /app
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Development

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY ["Smart_X_PROG/Smart_X_API.csproj", "Smart_X_PROG/"]
RUN dotnet restore "Smart_X_PROG/Smart_X_API.csproj"
COPY . .
WORKDIR /src/Smart_X_PROG
RUN dotnet build "Smart_X_API.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "Smart_X_API.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Smart_X_API.dll"]